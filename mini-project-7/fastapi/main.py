from typing import Union

from fastapi import FastAPI, Query
from pydantic import BaseModel
import pandas as pd
import sqlite3

import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)
path = './'

import os
import requests
import json
from typing import Optional
from datetime import datetime
import xml.etree.ElementTree as ET
import pandas as pd
import sys
from fastapi import HTTPException
sys.path.append(path)



# 더 필요한 라이브러리 추가 -------------
import emergency1218_2138 as em

naver_id, naver_key='basrjpnowz','IXJeSwizs1aDacp561uU7kQ7cE6xY0WBwkfiwrLT'

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
    path1:str
    hospital2: str
    addr2: str
    tel2: str
    eta2: str
    dist2: float
    fee2: int
    path2:str
    hospital3: str
    addr3: str
    tel3: str
    eta3: str
    dist3: float
    fee3: int
    path3:str

    
class Emergency(BaseModel):
    status: str
    lat: float
    lon: float

@app.get("/")
def read_root():
    return {"Hello": "World"}


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

@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}

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
        result_t = hospital_recommender.text_summary()
        # Classify situation
        transcript, result_r, ilat, ilon, result_t = hospital_recommender.classify_situation()
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
    
    return { 
            "datetime": log_instance.datetime,
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
            "path1":log_instance.path1,
            "hospital2": log_instance.hospital2,
            "addr2": log_instance.addr2,
            "tel2": log_instance.tel2,
            "eta2": log_instance.eta2,
            "dist2": log_instance.dist2,
            "fee2": log_instance.fee2,
            "path2":log_instance.path2,
            "hospital3": log_instance.hospital3,
            "addr3": log_instance.addr3,
            "tel3": log_instance.tel3,
            "eta3": log_instance.eta3,
            "dist3": log_instance.dist3,
            "fee3": log_instance.fee3,
            "path3":log_instance.path3
            }
            
# admin log 12/24 12:24  
from typing import Optional
       
class AdminLog (BaseModel):
    datetime: str
    input_text: str
    input_summary: str
    input_latitude: float
    input_longitude: float
    em_class: int
    hospital1: Optional[str] = ""
    addr1: Optional[str] = ""
    tel1: Optional[str] = ""
    eta1: Optional[str] = ""
    dist1: Optional[float] = 0
    fee1: Optional[float] = 0
    hospital2: Optional[str] = ""
    addr2: Optional[str] = ""
    tel2: Optional[str] = ""
    eta2: Optional[str] = ""
    dist2: Optional[float] = 0
    fee2: Optional[float] = 0
    hospital3: Optional[str] = ""
    addr3: Optional[str] = ""
    tel3: Optional[str] = ""
    eta3: Optional[str] = ""
    dist3: Optional[float] = 0
    fee3: Optional[float] = 0

@app.get('/items')
def getLogList(
    startDate: Optional[str] = Query(None), 
    endDate: Optional[str] = Query(None),
    emClass: Optional[int] = Query(None)
    ):
    conn = sqlite3.connect('./db/em.db')

    query = """
        SELECT datetime, input_text, input_summary, input_latitude, input_longitude, 
            em_class, hospital1, addr1, tel1, eta1, dist1, fee1, 
            hospital2, addr2, tel2, eta2, dist2, fee2, 
            hospital3, addr3, tel3, eta3, dist3, fee3 
        FROM request1
    """
    df1 = pd.read_sql(query, conn)
    
    query = """
        SELECT datetime, input_text, input_summary, input_latitude, input_longitude, 
            em_class, hospital1, addr1, tel1, eta1, dist1, fee1, 
            hospital2, addr2, tel2, eta2, dist2, fee2, 
            hospital3, addr3, tel3, eta3, dist3, fee3 
        FROM request2
    """
    df2 = pd.read_sql(query, conn)
    
    df = pd.concat([df1, df2], ignore_index=True)
    
    conn.close()
    
    result = []
    for _, row in df.iterrows():
        row_dict = row.to_dict()
        row_dict = {k: (None if pd.isna(v) else v) for k, v in row_dict.items()}
        
        log_instance = AdminLog(**row_dict)
        result.append({ "datetime": log_instance.datetime,
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
            })
        
        if startDate and endDate:
            start_date = datetime.strptime(startDate, "%Y-%m-%dT%H:%M:%S")
            end_date = datetime.strptime(endDate, "%Y-%m-%dT%H:%M:%S")
            result = [
                log for log in result 
                if start_date <= datetime.strptime(log["datetime"], "%Y-%m-%d %H:%M:%S") <= end_date
            ]

        if emClass in range(1,6):
            result = [
                log for log in result
                if log["em_class"] == emClass
            ]
        
    return result

### 12/24 10:09 홍규님 코드
from typing import Union

from fastapi import FastAPI, File, UploadFile,Form
from fastapi.responses import JSONResponse
import shutil
import os
from pydantic import BaseModel
import pandas as pd
import sqlite3
import logging
import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)
path = './'

import os
import requests
import xml.etree.ElementTree as ET
import pandas as pd
import sys
from fastapi import HTTPException

class Emergency_audio(BaseModel):
    lat: float
    lon: float



UPLOAD_DIR = "self_audio"
os.makedirs(UPLOAD_DIR, exist_ok=True)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.post("/upload-mp3/")
async def upload_mp3(emer: str = Form(...), file: UploadFile = File(...)):
    try:
        # emer를 위도와 경도로 분리
        lat, lon = map(float, emer.split(","))
        logger.info("lat&lon")
        logger.info(lat)
        logger.info(lon)
    except ValueError:
        logger.error(f"Invalid 'emer' format: {emer}")
        raise HTTPException(status_code=400, detail="Invalid 'emer' format. Use 'latitude,longitude' format.")
    
    if not file.filename.endswith(".mp3"):
        logger.error(f"Invalid file type: {file.filename}")
        return JSONResponse({"error": "Invalid file type. Only MP3 files are allowed."}, status_code=400)

    file_path = os.path.join(UPLOAD_DIR, file.filename)
    
    try:
        # Save the file locally
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        logger.info(f"File {file.filename} saved successfully at {file_path}")
    except Exception as e:
        logger.error(f"Error while saving the file: {str(e)}")
        raise HTTPException(status_code=500, detail="Error saving the file.")

    try:
        hospital_recommender = em.RecommendHospital3(
            naver_id=naver_id,
            naver_key=naver_key,
            path=path,
            filename=file.filename,
            latitude=lat,
            longitude=lon
        )
        hospital_recommender.load_api_key()
        result_t = hospital_recommender.text_summary()
        logger.info(result_t)
        transcript, result_r, ilat, ilon, result_t = hospital_recommender.classify_situation()
        result = hospital_recommender.search_map()
    except Exception as e:
        logger.error(f"Error during hospital recommender process: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")
    
    if isinstance(result, str):
        logger.info(f"Result: {result}")
        return {"result": result}

    if not result.empty:
        row_data = result.iloc[0].to_dict()
        log_instance = Log(**row_data)
    else:
        logger.warning("No hospital found.")
        raise HTTPException(status_code=404, detail="No hospital found.")
    
    return {
        "datetime": log_instance.datetime,
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
        "path1": log_instance.path1,
        "hospital2": log_instance.hospital2,
        "addr2": log_instance.addr2,
        "tel2": log_instance.tel2,
        "eta2": log_instance.eta2,
        "dist2": log_instance.dist2,
        "fee2": log_instance.fee2,
        "path2": log_instance.path2,
        "hospital3": log_instance.hospital3,
        "addr3": log_instance.addr3,
        "tel3": log_instance.tel3,
        "eta3": log_instance.eta3,
        "dist3": log_instance.dist3,
        "fee3": log_instance.fee3,
        "path3": log_instance.path3

    }
