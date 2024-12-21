###############################################################
## 해당 파일은 로컬 cam을 이용하여 본인의 이미지를 촬영하고 저장합니다.
###############################################################

import os
import cv2

## 이미지를 저장할 폴더 경로
folder_path = './my_face'
## 이미지 저장 폴더가 없다면 폴더 생성
if not os.path.exists(folder_path) :
    os.makedirs(folder_path)
    print('my_face 폴더를 생성합니다.')

## 웹캠으로 얼굴 사진을 찍어 저장하는 함수
def capture_owner_images(num_images=1000) : ## num_images에 숫자를 입력한만큼 이미지 저장
    ## 0은 기본 웹캠
    cap = cv2.VideoCapture(0)
    ## haarcascade 알고리즘으로 얼굴 탐지
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

    ## 위에서 지정한 수만큼 촬영 및 저장하는 반복문
    count = 0
    while count < num_images :
        _, frame = cap.read()
        
        frame = cv2.flip(frame, 1)
        ## haarcascade 알고리즘은 흑백 이미지의 명암 차이로 탐지를 하는 것이기에 흑백으로 변환
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        ## 변환된 프레임에서 얼굴 탐지 시도
        faces = face_cascade.detectMultiScale(gray, 1.1, 4)

        ## 탐지된 얼굴에서 좌표를 가져오는 반복문
        for (x, y, w, h) in faces :
            ## 프레임에서 얼굴 영역만 가져온다
            face = frame[y:y+h, x:x+w]
            ## 탐지한 얼굴 영역 사이즈 변환 : 괄호를 채우면 됩니다.
            resized_face = cv2.resize(face, (160, 160) )
            ## 파일 이름 설정 및 저장
            face_file = f"{folder_path}/my_face_{count+10000}.jpg"
            cv2.imwrite(face_file, resized_face)
            count += 1
            ## 변환된 얼굴 이미지 출력하여 확인
            cv2.imshow('Captured Face', resized_face)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()
    
capture_owner_images()
print('저장 완료')