from fastapi import FastAPI, Query, Request
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.templating import Jinja2Templates
from emergency import RecommendHospital3ByInput

app = FastAPI()
templates = Jinja2Templates(directory="templates")

@app.get("/")
def hospital_input_page(request: Request):
    return templates.TemplateResponse(
        "hospital_search.html", {"request": request})

@app.get("/hospital_by_module")
def hospital_list(
    request: Request,
    input_text: str = Query(..., description="사용자의 입력 텍스트"),
    latitude: float = Query(..., description="위도 값"),
    longitude: float = Query(..., description="경도 값")
):
    hospital_recommender = RecommendHospital3ByInput(input_text, latitude, longitude)
    
    hospitals = hospital_recommender.search_map()

    # return templates.TemplateResponse(
    #     "hospital_list.html",
    #     {
    #         "request": request,
    #         "hospitals": hospitals
    #     }
    # )
    
    return JSONResponse(content=hospitals, media_type="application/json")