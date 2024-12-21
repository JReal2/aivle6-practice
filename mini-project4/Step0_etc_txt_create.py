################################################################
## 해당 파일은 haarcascades 알고리즘으로 얼굴을 촬영하고 저장하였을 때,
## 각 이미지 파일 이름에 맞추어 txt 파일을 생성합니다.
################################################################

import os
import glob

## 이미지 파일이 있는 경로
folder_path = 'Datasets/Keras/images/my_face/'

## YOLO 형식의 annotation (class, x, y, w, h)
annotation = '1 0.5 0.5 1 1'

## 이미지 파일 확장자 목록
img_extensions = ['.jpg', '.jpeg', '.png']

## 이미지 파일 목록 생성
img_files = []
for ext in img_extensions :
    img_files.extend( glob.glob(os.path.join(folder_path, f'*{ext}')) )
    
## 이미지 파일별로 동일한 이름의 txt 파일 생성
for img_file in img_files :
    ## 이미지 파일 이름에서 확장자를 제거하고 .txt 확장자로 변환
    txt_file = os.path.splitext(img_file)[0] + '.txt'
    
    ## 해당 txt 파일에 YOLO annotation 내용 작성
    with open(txt_file, 'w') as f :
        f.write(annotation + '\n')
        
    print(f'{txt_file} 파일 생성 완료')
print('모든 txt 파일이 생성되었습니다.')