from typing import Union

from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import sqlite3

import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)
path = './'

import os
import json
import requests
import xml.etree.ElementTree as ET
import pandas as pd
import sys
from fastapi import HTTPException
sys.path.append(path)



# 더 필요한 라이브러리 추가 -------------
import emergency1218_2138 as em

filepath = 'map_key.txt'    
with open(filepath, 'r') as file:
    map_key = json.load(file)
    naver_id, naver_key = map_key['c_id'], map_key['c_key']

app = FastAPI()


class Log (BaseModel):
    datetime: str
    input_text: str
    input_summary: str
    input_latitude: float
    input_longitude: float
    em_class: int
    hospital1: str
    addr1: str
    tel1: str
    eta1: str
    dist1: float
    fee1: int
    hospital2: str
    addr2: str
    tel2: str
    eta2: str
    dist2: float
    fee2: int
    hospital3: str
    addr3: str
    tel3: str
    eta3: str
    dist3: float
    fee3: int

    
class Emergency(BaseModel):
    status: str
    lat: float
    lon: float

@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}


@app.put("/items/{filename}")
def update_item(filename: str):
    try:
        hospital_recommender = em.RecommendHospital3(naver_id=naver_id,
                                                        naver_key=naver_key,
                                                        path=path,
                                                        filename=filename
                                                        )
        hospital_recommender.load_api_key()
        result_t = hospital_recommender.text_summary()
        transcript, result_r, ilat, ilon, result_t = hospital_recommender.classify_situation()
        result = hospital_recommender.search_map()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")
    
    if isinstance(result, str):
         return {"result": result}

    if not result.empty:
        row_data = result.iloc[0].to_dict()
        log_instance = Log(**row_data)
    else:
        raise HTTPException(status_code=404, detail="No hospital found.")
        
    return { "datetime": log_instance.datetime,
            "input_text": log_instance.input_text,
            "input_summary": log_instance.input_summary,
            "input_latitude": log_instance.input_latitude,
            "input_longitude": log_instance.input_longitude,
            "em_class": log_instance.em_class,
            "hospital1": log_instance.hospital1,
            "addr1": log_instance.addr1,
            "tel1": log_instance.tel1,
            "eta1": log_instance.eta1,
            "dist1": log_instance.dist1,
            "fee1": log_instance.fee1,
            "hospital2": log_instance.hospital2,
            "addr2": log_instance.addr2,
            "tel2": log_instance.tel2,
            "eta2": log_instance.eta2,
            "dist2": log_instance.dist2,
            "fee2": log_instance.fee2,
            "hospital3": log_instance.hospital3,
            "addr3": log_instance.addr3,
            "tel3": log_instance.tel3,
            "eta3": log_instance.eta3,
            "dist3": log_instance.dist3,
            "fee3": log_instance.fee3
            }


@app.post("/items/text")
def update_item_text(emer: Emergency):
    try:
        # Create hospital recommender object
        hospital_recommender = em.RecommendHospital3(naver_id=naver_id,
                                                     naver_key=naver_key,
                                                     path=path,
                                                     transcript=emer.status,
                                                     latitude=emer.lat,
                                                     longitude=emer.lon)
        # Load API key
        hospital_recommender.load_api_key()
        # Get text summary
        # result_t = hospital_recommender.text_summary()
        # Classify situation
        # transcript, result_r, ilat, ilon, result_t = hospital_recommender.classify_situation()
        # Search for hospitals
        result = hospital_recommender.search_map()
 

    except Exception as e:
        print(result.head())
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")
    
    if isinstance(result, str):
        return {"result": result}

    if not result.empty:
        row_data = result.iloc[0].to_dict()
        log_instance = Log(**row_data)
    else:
        raise HTTPException(status_code=404, detail="No hospital found.")
    
    return { "datetime": log_instance.datetime,
            "input_text": log_instance.input_text,
            "input_summary": log_instance.input_summary,
            "input_latitude": log_instance.input_latitude,
            "input_longitude": log_instance.input_longitude,
            "em_class": log_instance.em_class,
            "hospital1": log_instance.hospital1,
            "addr1": log_instance.addr1,
            "tel1": log_instance.tel1,
            "eta1": log_instance.eta1,
            "dist1": log_instance.dist1,
            "fee1": log_instance.fee1,
            "hospital2": log_instance.hospital2,
            "addr2": log_instance.addr2,
            "tel2": log_instance.tel2,
            "eta2": log_instance.eta2,
            "dist2": log_instance.dist2,
            "fee2": log_instance.fee2,
            "hospital3": log_instance.hospital3,
            "addr3": log_instance.addr3,
            "tel3": log_instance.tel3,
            "eta3": log_instance.eta3,
            "dist3": log_instance.dist3,
            "fee3": log_instance.fee3
            }
            
            