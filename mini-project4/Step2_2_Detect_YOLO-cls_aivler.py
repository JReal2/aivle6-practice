import cv2
import numpy as np
from mtcnn import MTCNN
from ultralytics import YOLO



###############################################
## 모델 불러오기 : 무엇을 불러와야 할까요?
model = YOLO('./best.pt')
###############################################

## opencv에서 사용하려는 카메라
cap = cv2.VideoCapture(0)

## Haar Cascades, Viola–Jones object detection framework
## https://en.wikipedia.org/wiki/Viola%E2%80%93Jones_object_detection_framework
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

## 카메라 동작 확인용
if not cap.isOpened() :
    print('웹캠 실행 불가')
    exit()

## 매 프레임마다 동작시킬 것이므로 무한 반복문
while True :
    ret, frame = cap.read()
    if not ret :
        print('프레임 로드 불가')
        break
    
    frame = frame.astype(np.uint8)
    ## 카메라 좌우 전환
    frame = cv2.flip(frame, 1)
    
    ## Haar Cascades은 흑백으로 된 이미지에서 명암 차이로 특징을 추출
    ## 때문에 컬러 이미지를 흑백 이미지로 전환
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    ## Haar Cascades 알고리즘으로 흑백 이미지에서 얼굴 탐색
    faces = face_cascade.detectMultiScale(gray_frame)
    
    ## 탐색된 얼굴들의 좌표를 가져옴
    for (x,y,w,h) in faces :
        face = frame[y:y+h, x:x+w]  ## OpenCV는 이미지를 행렬로 저장하기에 y, x 순
        face_rgb = cv2.cvtColor(face, cv2.COLOR_BGR2RGB)
        
        ## 예측값 생성 : 무엇이 들어가야 할까요?
        results = model(face_rgb)
        
        ## 예측값을 이용한 반복문
        for r in results :
            ## 확률이 가장 높은 클래스가 0이라면, 초록색 박스 그림
            if r.probs.top1==0 :
                color = (0,255,0)
                prob = float(r.probs.top1conf)*100
                label_text = f'My Face prob : {prob:.2f}%'
            ## 그렇지 않다면, 빨간색 박스 그림p
            else :
                color = (0,0,255)
                prob = float(r.probs.top1conf)*100
                label_text = f'Other Face prob : {prob:.2f}%'
        
        ## 프레임에 꼭짓점 2개의 좌표를 이용하여 박스를 표현해줌
        cv2.rectangle(frame, (x, y), (x+w , y+h), color, 2)
        
        ## 박스 위에 텍스트 추가
        cv2.putText(frame, label_text, (x, y-10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7,
                    color, 2,
                    )
    
    ## 프레임을 확인할 수 있다
    cv2.imshow('Face_Detection', frame)
    
    ## 키보드 q 키를 누르면 반복문 종료
    if cv2.waitKey(1) & 0xFF == ord('q') :
        break
    
cap.release()
cv2.destroyAllWindows()