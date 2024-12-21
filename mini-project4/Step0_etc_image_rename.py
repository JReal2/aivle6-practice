##############################################################
## 해당 파일은 폴더 내부 이미지 파일의 이름을 재정리 할 때 사용합니다.
##############################################################

import os

## 이름을 정리할 이미지가 있는 경로
folder_path = ''
## 이미지 경로의 파일들을 리스트화
file_list = os.listdir(folder_path)

## 폴더 생성
if not os.path.exists(folder_path) :
    os.makedirs(folder_path)
    print('폴더를 생성합니다.')

print('폴더 안의 이미지명을 정리합니다.')

## 파일명 리스트를 하나하나 반복
for f_name in file_list :
    before_name = os.path.join(folder_path, f_name)
    
    idx = 0
    new_name = f'파일명_{idx}.jpg'
    after_name = os.path.join(folder_path, new_name)
    
    while os.path.exists(after_name) :  
        idx += 1
        new_name = f'파일명_{idx}.jpg'
        after_name = os.path.join(folder_path, new_name)

    ## 이름 변경
    os.rename(before_name, after_name)
    
print('정리 완료.')