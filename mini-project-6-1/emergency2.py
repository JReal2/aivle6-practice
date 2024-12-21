## 경로 수정 필요

import os
import pandas as pd
import numpy as np

from haversine import haversine
import requests
import json

import xml.etree.ElementTree as ET
import matplotlib.pyplot as plt
import openai
from openai import OpenAI
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
from geopy.distance import geodesic
from geopy import Point
from haversine import haversine,haversine_vector, Unit
from warnings import filterwarnings
FutureWarning
filterwarnings('ignore')

class RecommendHospital3:
    def __init__(self, filename, naver_id, naver_key, path):
        self.filename = filename
        self.th = 3
        self.id = naver_id
        self.key = naver_key
        self.path = path
        self.data = pd.read_csv(self.path + '응급실 정보.csv', encoding='utf-8') ## 수정 필요
        self.audio_path = path + 'self_audio/' ## 수정 필요

    # 0. load key file------------------
    def load_api_key(self):
      filepath = self.path + 'api_key.txt'
      with open(filepath, 'r') as file:
        openai.api_key = file.readline().strip()
        os.environ['OPENAI_API_KEY'] = openai.api_key

    # 1 audio2text2summary--------------------
    def audio_summary(self):
      location = pd.read_excel(self.path + 'audio_location.xlsx')
      latitude = location['위도'].loc[location['filename'] == self.filename].iloc[0]
      longitude = location['경도'].loc[location['filename'] == self.filename].iloc[0]

      # OpenAI 클라이언트 생성
      client = OpenAI()

      audio_file = open(self.audio_path + self.filename, "rb")
      transcript = client.audio.transcriptions.create(
          file=audio_file,
          model="whisper-1",
          language="ko",
          response_format="text",
      )

      # 시스템 역할과 응답 형식 지정
      system_role = '''당신은 의사입니다.
      당신은 응급상황이 발생했을 때, 119 신고자의 전화 통화 내용을 듣고, 더 실력있는 의사에게 응급상황인지 아닌지 구별할 수 있게끔 문장을 가다듬는 업무를 하고 있습니다.
      응답은 다음의 형식을 지켜주세요.
      응급상황인지 아닌지를 구별할 수 있게끔 문장을 제공해야 합니다. (ex) 숨을 쉬지 않음, 경미한 발열, 가벼운 탈수 증세 등)
      신고자는 정확한 정보를 제공할 수도, 아닐 수도 있습니다.(정말 긴급한 상황에서의 구급차 요청, 장난전화로 구급차 요청)
      구급차를 제공하는 기준은 한국 응급환자 중증도 분류기준에 따라 1,2,3등급에 해당하면 구급차를 제공합니다.
      구급차를 제공하지 않는 기준은 한국 응급환자 중증도 분류기준에 따라 4, 5등급에 해당하면 구급차를 제공하지 않습니다.
      문장에는 다음의 정보를 포함할 수 있습니다. (ex) 증상, 증세, 각종 지병, 신고자의 주관적인 판단(ex)신고자는 위급상황이라고 판단하고 있습니다.), 상황을 파악할 수 있는 객관적인 사실 등)
      문장에는 다음의 정보를 포함할 수 없습니다. (ex) 신고자의 위치)

      (예시) Python 3
      text = '손을 살짝 베었는데, 구급차 불러주세요.'
      test = text_summary(text)
      print(test)
      => {"상황": \"환자는 손을 살짝 베였습니다.\"}
      => {"환자의 요구, 질문 및 판단": \"구급차를 요청하고 있습니다. \"}
      => {"1차 판단": \"손을 살짝 베었기 때문에, 간단한 조치만 취하면 될 것 같습니다. \"}

      이 때 1차 판단에 해당되는 내용만 출력해주세요.
      '''

      # 입력데이터를 GPT-3.5-turbo에 전달하고 답변 받아오기
      response = client.chat.completions.create(
          model="o1-preview",
          messages=[
              {
                  "role": "system",
                  "content": system_role
              },
              {
                  "role": "user",
                  "content": transcript
              }
          ]
      )

      # 응답 받기
      answer = response.choices[0].message.content
      print(transcript, answer)

      return answer, latitude, longitude

    
    # 2. model prediction------------------
    def classify_situation(self):
      text, latitude, longitude = self.audio_summary()

      save_directory = self.path + 'fine_tuned_bert/2/' ## 수정 필요

      # 모델 로드
      model = AutoModelForSequenceClassification.from_pretrained(save_directory)

      # 토크나이저 로드
      tokenizer = AutoTokenizer.from_pretrained(save_directory)

      device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

      # 입력 문장 토크나이징
      inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
      inputs = {key: value.to(device) for key, value in inputs.items()}  # 각 텐서를 GPU로 이동

      # 모델 예측
      with torch.no_grad():
          outputs = model(**inputs)

      # 로짓을 소프트맥스로 변환하여 확률 계산
      logits = outputs.logits
      probabilities = logits.softmax(dim=1)

      # 가장 높은 확률을 가진 클래스 선택
      pred = torch.argmax(probabilities, dim=-1).item() + 1
      print(pred, '등급')

      return pred, latitude, longitude

    # 3-1. get_distance------------------
    def km_to_lat_lon(self, km_north, km_east, latitude=37.5665, longitude=126.978):
        start_point = Point(latitude, longitude)
        north_point = geodesic(kilometers=km_north).destination(start_point, bearing=0)
        east_point = geodesic(kilometers=km_east).destination(north_point, bearing=90)
        return east_point.latitude - latitude, east_point.longitude - longitude

    def get_dist(self, start_lat, start_lng, dest_lat, dest_lng):
        url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving"
        headers = {
            "X-NCP-APIGW-API-KEY-ID": self.id,
            "X-NCP-APIGW-API-KEY": self.key,
        }
        params = {
            "start": f"{start_lng},{start_lat}",
            "goal": f"{dest_lng},{dest_lat}",
            "option": "trafast"
        }

        response = requests.get(url, headers=headers, params=params)
        if response.status_code == 200:
            response_data = response.json()
            try:
                return response_data['route']['trafast'][0]['summary']
            except KeyError:
                return None
        else:
            return None

    def convert_milliseconds(self, milliseconds):
        hours = milliseconds // (1000 * 60 * 60)
        minutes = (milliseconds // (1000 * 60)) % 60
        return hours, minutes

    # 3-2. recommendation------------------
    def recommend_hospital(self):
        pred, lat, lon = self.classify_situation()

        if pred > 3:
          return None, lat, lon

        x, y = self.km_to_lat_lon(self.th, self.th, lat, lon)

        filter_lst = self.data.loc[
            ((lat - x < self.data['위도']) &
             (self.data['위도'] < lat + x)) &
            ((lon - y < self.data['경도']) &
             (self.data['경도'] < lon + y))
        ].copy()

        while len(filter_lst) < 5:
            self.th += 5
            x, y = self.km_to_lat_lon(self.th, self.th, lat, lon)
            filter_lst = self.data.loc[
                ((lat - x < self.data['위도']) &
                 (self.data['위도'] < lat + x)) &
                ((lon - y < self.data['경도']) &
                 (self.data['경도'] < lon + y))
            ].copy()

            if len(filter_lst) >= 5:
                break

        if filter_lst.empty:
            return None

        locations = list(zip(filter_lst['위도'], filter_lst['경도']))
        filter_lst['distance'] = haversine_vector(
            locations,
            np.tile([lat, lon], (len(filter_lst), 1)),
            unit=Unit.KILOMETERS
        )
        return filter_lst.sort_values(by='distance').head(5), lat, lon

    def search_map(self):
        filter_lst, lat, lon = self.recommend_hospital()
        if filter_lst is None:
            return "가까운 병원을 찾아가는 것을 추천드립니다."

        total_result = {'목적지': [], '경과시간': [], '거리': []}

        for i in range(len(filter_lst)):
            hospital = filter_lst.iloc[i]
            result = self.get_dist(
                lat, lon,
                hospital['위도'], hospital['경도']
            )
            if result:
                total_result['목적지'].append(hospital['병원이름'])
                hours, minutes = self.convert_milliseconds(result['duration'])
                total_result['경과시간'].append(f"{hours}시간 {minutes}분")
                total_result['거리'].append(result['distance'])

        return total_result
