import glob
import os

# 대상 폴더 경로 설정
folder_path = 'labels/'  # 원하는 폴더 경로로 바꾸세요.

# 폴더 내 모든 txt 파일 탐색
txt_files = glob.glob(os.path.join(folder_path, "*.txt"))

# 각 파일을 순회하며 수정
for file_path in txt_files:
    with open(file_path, 'r+') as file:
        content = file.read()
        
        # 첫 글자가 '0'일 경우에만 '1'로 변경
        if content and content[0] == '0':
            content = '1' + content[1:]
            
            # 파일 쓰기 (처음부터 다시 기록)
            file.seek(0)
            file.write(content)
            file.truncate()  # 기존 내용보다 짧아질 수 있으므로 잘라냄
