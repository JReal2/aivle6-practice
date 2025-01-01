import os
import pandas as pd
import numpy as np

from haversine import haversine
import requests
import json

import xml.etree.ElementTree as ET
import openai
from openai import OpenAI
from transformers import AutoTokenizer, AutoModelForSequenceClassification
from sentence_transformers import SentenceTransformer, util

import torch
from geopy.distance import geodesic
from geopy import Point
import sqlite3
from haversine import haversine,haversine_vector, Unit
from datetime import datetime
from warnings import filterwarnings
FutureWarning
filterwarnings('ignore')

class RecommendHospital3:
    def __init__(self, naver_id, naver_key, path, filename=None, transcript=None, latitude=None, longitude=None):
        self.th = 3
        self.id = naver_id
        self.key = naver_key
        self.path = path
        self.data = pd.read_csv(self.path + '응급실 정보.csv', encoding='utf-8')

        # filename 초기화
        if filename:
            self.filename = filename
            self.audio_path = path + 'self_audio/'  
            self.lat = latitude
            self.lon = longitude
        elif transcript is not None and latitude is not None and longitude is not None:
            self.text = transcript
            self.lat = latitude
            self.lon = longitude
        else:
            raise ValueError("Either 'filename' or all of 'transcript', 'latitude', and 'longitude' must be provided.")
        
    # 0. load key file------------------
    def load_api_key(self):
      filepath = self.path + 'api_key.txt'
      with open(filepath, 'r') as file:
          openai.api_key = file.readline().strip()
          os.environ['OPENAI_API_KEY'] = openai.api_key

    # 1 audio2text2summary--------------------
    def audio_to_text(self):
      latitude = self.lat
      longitude = self.lon

      # OpenAI 클라이언트 생성
      client = OpenAI()

      audio_file = open(self.audio_path + self.filename, "rb")
      transcript = client.audio.transcriptions.create(
          file=audio_file,
          model="whisper-1",
          language="ko",
          response_format="text",
      )
      
      return transcript, latitude, longitude

    def text_summary(self):
        if not hasattr(self, "text"):
            # audio_to_text 호출을 통해 값을 초기화
            transcript, latitude, longitude = self.audio_to_text()
        else:
            # 이미 멤버 변수로 값이 설정된 경우
            transcript = self.text
            latitude = self.lat
            longitude = self.lon
            
        client = OpenAI()
        
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
        => {"1차 판단": \"손을 살짝 베었기 때문에, 간단한 조치만 취하면 될 것 같습니다. \"}
        '''

        # 입력데이터를 GPT-3.5-turbo에 전달하고 답변 받아오기
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
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

        return transcript, answer, latitude, longitude

    
    # 2. model prediction------------------
    def classify_situation(self):
      transcript, text, latitude, longitude = self.text_summary()
      
      # =====
      # RAG
      emb_directory = self.path + 'RAG_model/'
      
      embedder = SentenceTransformer(emb_directory)
      corpus = []
      
      RAG_docs_path = self.path + "RAG_docs.txt"
      with open(RAG_docs_path, "r", encoding="utf-8") as file:
          for line in file:
            line = line.strip()  # 양쪽 공백 제거
            if line:  # 빈 줄은 제외
                corpus.append(line)
      # 임베딩 캐싱 경로 설정
      cache_path = './corpus_embeddings_cache.pt'

      # 캐싱 확인 및 로드
      if not os.path.exists(cache_path):
          corpus_embeddings = embedder.encode(corpus, convert_to_tensor=True, batch_size=32)
          torch.save(corpus_embeddings, cache_path)
          print("임베딩 캐싱 완료.")
      else:
          corpus_embeddings = torch.load(cache_path)
          print("임베딩 캐시 로드 완료.")
      

            
      top_k = 5
      query_embedding = embedder.encode(text, convert_to_tensor=True)
      cos_scores = util.pytorch_cos_sim(query_embedding, corpus_embeddings)[0]
      cos_scores = cos_scores.cpu()
    
      #We use np.argpartition, to only partially sort the top_k results
      top_results = np.argpartition(-cos_scores, range(top_k))[0:top_k]
      response = f"""Query: {text}\n
      Corpus[1] : {corpus[top_results[0]]}\n
      Corpus[2] : {corpus[top_results[1]]}\n
      Corpus[3] : {corpus[top_results[2]]}\n
      Corpus[4] : {corpus[top_results[3]]}\n
      Corpus[5] : {corpus[top_results[4]]}\n
      """
      print(response)
      # =====

      save_directory = self.path + 'fine_tuned_bert_v2/'

      # 모델 로드
      model = AutoModelForSequenceClassification.from_pretrained(save_directory)

      # 토크나이저 로드
      tokenizer = AutoTokenizer.from_pretrained(save_directory)

      device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

      # 입력 문장 토크나이징
      inputs = tokenizer(response, return_tensors="pt", truncation=True, padding=True)
      inputs = {key: value.to(device) for key, value in inputs.items()}  # 각 텐서를 GPU로 이동

      # 모델 예측
      with torch.no_grad():
          outputs = model(**inputs)

      # 로짓을 소프트맥스로 변환하여 확률 계산
      logits = outputs.logits
      probabilities = logits.softmax(dim=1)

      # 가장 높은 확률을 가진 클래스 선택
      pred = torch.argmax(probabilities, dim=-1).item() + 1

      return transcript, pred, latitude, longitude, text # text 추가

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
                return response_data['route']['trafast'][0]['summary'],response_data['route']['trafast'][0]['path']
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
        transcript, pred, lat, lon, text = self.classify_situation()

        if pred > 3:
          return transcript, None, lat, lon, pred, text

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
        
        return transcript, filter_lst.sort_values(by='distance').head(3), lat, lon, pred, text

    def search_map(self):
        input_text, filter_lst, lat, lon, pred, text = self.recommend_hospital()
        
        dt = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        total_result = {
            "datetime": dt,
            "input_text": input_text,
            "input_summary": text,
            "input_latitude": lat,
            "input_longitude": lon,
            "em_class": pred,
            "hospital1": None,
            "addr1": None,
            "tel1": None,
            "eta1": None,
            "dist1": None,
            "fee1": None,
            "path1":None,
            "hospital2": None,
            "addr2": None,
            "tel2": None,
            "eta2": None,
            "dist2": None,
            "fee2": None,
            "path2":None,
            "hospital3": None,
            "addr3": None,
            "tel3": None,
            "eta3": None,
            "dist3": None,
            "fee3": None,
            "path3":None,
        }
        
        if filter_lst is None:
            total_result = pd.DataFrame([total_result])
            self.send_data2(total_result)
            return "가까운 병원을 찾아가는 것을 추천드립니다."

        for i in range(len(filter_lst)):
            hospital = filter_lst.iloc[i]
            result, em_path = self.get_dist(
                lat, lon,
                hospital['위도'], hospital['경도']
            )
            if result:
                hours, minutes = self.convert_milliseconds(result['duration'])
                
                hospital_key = f"hospital{i+1}"
                addr_key = f"addr{i+1}"
                tel_key = f"tel{i+1}"
                eta_key = f"eta{i+1}"
                dist_key = f"dist{i+1}"
                fee_key = f"fee{i+1}"
                path_key = f"path{i+1}"

                total_result[hospital_key] = hospital["병원이름"]
                total_result[addr_key] = hospital["주소"]
                total_result[tel_key] = hospital["전화번호 1"]
                total_result[eta_key] = (f"{hours}시간 {minutes}분")
                total_result[dist_key] = result['distance'] / 1000
                total_result[fee_key] = int(result['taxiFare']) + int(result['tollFare'])
                total_result[path_key] = str(em_path)
        
        total_result = pd.DataFrame([total_result])        
        self.send_data1(total_result)
        
        return total_result
    
    def send_data1(self, data): # 1 ~ 3 등급에 관한 DB에 추가
        path = './db/em.db'
        with sqlite3.connect(path) as condb:
            conn = sqlite3.connect(path)
            data.to_sql('request1', condb, if_exists='append', index=False)
            conn.close()
            print('1~3등급 DB에 성공적으로 추가하였습니다.')
            
    def send_data2(self, data): # 4 ~ 5 등급에 관한 DB에 추가
        path = './db/em.db'
        with sqlite3.connect(path) as condb:
            conn = sqlite3.connect(path)
            data.to_sql('request2', condb, if_exists='append', index=False)
            conn.close()
            print('4~5등급 DB에 성공적으로 추가하였습니다.')
