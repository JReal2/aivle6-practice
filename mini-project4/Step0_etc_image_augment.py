################################################
## 해당 파일은 이미지 augmentation 작업을 수행합니다.
################################################

import cv2
import keras

import os
import numpy as np

keras.utils.clear_session()

#####################################################
## augmentation 방법 설정 : keras.layers에서 찾아보세요!
aug_layers = keras.models.Sequential()
# aug_layers.add( keras.layers.RandomFlip() )
# aug_layers.add( keras.layers.RandomRotation(0.15) )
# aug_layers.add( keras.layers.RandomZoom(0.15) )
#####################################################

## augmentation을 적용할 원본 이미지가 있는 경로
img_folder = ''
img_files = os.listdir(img_folder)

## augmentation을 적용한 이미지를 저장할 폴더 경로 및 생성
output_folder = './augmented_images'
if not os.path.exists(output_folder) :
    os.mkdir(output_folder)
    
## 원본 이미지 경로로 만들어진 원본 이미지 리스트를 이용한 반복문
for img_f in img_files :
    ## 원본 이미지 경로
    img_ori = os.path.join(img_folder, img_f)
    ## 이미지 선택 및 array화
    img_ori = keras.utils.load_img(img_ori)
    img_ori = keras.utils.img_to_array(img_ori)
    
    ## 이미지 개별마다 몇 개의 augment를 진행할 것인지에 대한 반복문
    for i in range() :  ## 생성할 이미지 수 입력 필요
        ## 새롭게 저장될 이미지 파일명
        aug_path = os.path.join(output_folder, f'aug_{i}_{img_f}')
        
        ##################################
        ## 위에서 설정한 augment 옵션들을 적용
        img_aug = aug_layers( img_ori )
        ##################################
        
        ## augment 작업을 거치면 자료형이 tensor로 바뀌어서 다시 array로 전환
        ## numpy array가 아니면 저장할 때 에러 발생
        img_aug = keras.utils.img_to_array( img_aug )
        ## cv2의 색상 정보 순서는 BGR순이기에 RGB로 전환
        img_aug = cv2.cvtColor(img_aug, cv2.COLOR_BGR2RGB)
        ## cv2를 이용하여 파일 저장
        cv2.imwrite(aug_path, img_aug)
        print(f'이미지 {img_f}에 대한 {i+1}번째 증강 작업 완료')
    print( f'이미지 {img_f} 증강 완료')
    print('========================')
print(f'이미지 전체 증강 작업 완료')