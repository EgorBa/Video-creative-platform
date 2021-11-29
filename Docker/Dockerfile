FROM python:3.7

EXPOSE 8080


COPY ./requirements.txt /inference-api/requirements.txt
RUN pip install -r /inference-api/requirements.txt

#COPY ./models.tar.gz /inference-api/
#RUN tar -zxf /inference-api/models.tar.gz --directory /inference-api
#RUN rm /inference-api/models.tar.gz

COPY ./inference-api/* /inference-api/inference-api/
COPY server.py /inference-api/server.py
COPY ./config.yml /inference-api/config.yml

WORKDIR /inference-api/

CMD [ "python", "/inference-api/server.py" ]