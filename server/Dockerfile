
FROM python:3-onbuild
RUN apt-get update && apt-get install -y tesseract-ocr
EXPOSE 80
CMD [ "python", "./server.py" ]
