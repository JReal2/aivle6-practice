import cv2
import numpy as np
import keras



## 해당 py 파일 필요
from Step0_etc_pretrained_def import scaling, conv2d_bn, _generate_layer_name, _inception_resnet_block

###############################################
## 모델 불러오기 : 무엇을 불러와야 할까요?
model = keras.saving.load_model('./step1_1.keras')
###############################################

## opencv에서 사용하려는 카메라
cap = cv2.VideoCapture(0)

## Haar Cascades, Viola–Jones object detection framework
## https://en.wikipedia.org/wiki/Viola%E2%80%93Jones_object_detection_framework
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

## 카메라 동작 확인용
if not cap.isOpened() :
    print('카메라 실행 불가')
    exit()

## 매 프레임마다 동작시킬 것이므로 무한 반복문
while True :
    
    ret, frame = cap.read()
    if not ret :
        print('프레임 로드 불가')
        break
    
    ## 카메라 좌우 전환
    frame = cv2.flip(frame, 1)
    
    ## Haar Cascades은 흑백으로 된 이미지에서 명암 차이로 특징을 추출
    ## 때문에 컬러 이미지를 흑백 이미지로 전환
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    ## Haar Cascades 알고리즘으로 흑백 이미지에서 얼굴 탐색
    faces = face_cascade.detectMultiScale(gray_frame)
    
    ## 탐색된 얼굴들의 좌표를 가져옴
    for (x,y,w,h) in faces :
        ## 프레임의 크기를 리사이즈 : 몇으로 해야 할까요?
        face_resized = cv2.resize(frame, (160, 160) )
        ## 숫자값을 0 ~ 1로 노말라이즈
        face_resized = face_resized / 255.0
        ## 모델에 전달하기 위해 차원 추가 : numpy
        face_resized = np.expand_dims(face_resized, axis=0)
        
        ## 예측값 생성 : 무엇이 들어가야 할까요?
        y_pred = model.predict(face_resized)
        
        ## 임계값 설정
        ## 예측값이 0.5 이상이면 초록색 박스
        if y_pred[0][0] >= 0.5 :
            color = (0,255,0)
            prob = y_pred[0][0]*100
            label_text = f'My Face prob : {prob:.2f}%'
        ## 그렇지 않으면 빨간색 박스
        else :
            color = (0,0,255)
            prob = y_pred[0][0]*100
            label_text = f'Other Face prob : {prob:.2f}%'
        
        # 프레임에 꼭짓점 2개의 좌표를 이용하여 박스를 표현해줌
        cv2.rectangle(frame, (x,y), (x+w, y+h), color, 2)
        
        ## 박스 위에 텍스트 추가
        cv2.putText(frame, label_text, (x, y-10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6,
                    color, 2,
                    )
    
    
    ## 프레임을 확인할 수 있다
    cv2.imshow('Face_Detection', frame)
    
    ## 키보드 q 키를 누르면 반복문 종료
    if cv2.waitKey(1) & 0xFF == ord('q') :
        break

cap.release()
cv2.destroyAllWindows()