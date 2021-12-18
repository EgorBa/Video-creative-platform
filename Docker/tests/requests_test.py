import requests
import json
import io
from PIL import Image


# This test is about sending requests to the server.

def send_api_request(input_path, output_path):
    try:
        url = 'http://{0}:{1}/method1'.format('localhost', 8080)
        return requests.post(url, json={'input_path': input_path, 'output_path': output_path})
    except requests.exceptions.RequestException as e:
        print(e)


def send_api_request_bytes(input_path):
    try:
        imageFileObj = open(input_path, 'rb')
        imageBinaryBytes = imageFileObj.read()
        imageStream = io.BytesIO(imageBinaryBytes)
        s = imageStream.read().decode('ISO-8859-1')
        url = 'http://{0}:{1}/cutout'.format('localhost', 8080)
        return requests.post(url, json={'img': s})
    except requests.exceptions.RequestException as e:
        print(e)


def send_api_requests_bytes(paths):
    try:
        url = 'http://{0}:{1}/cutouts'.format('localhost', 8080)
        imgs = {"imgs": []}
        for i in paths:
            imageFileObj = open(i, 'rb')
            imageBinaryBytes = imageFileObj.read()
            imageStream = io.BytesIO(imageBinaryBytes)
            s = imageStream.read().decode('ISO-8859-1')
            imgs["imgs"].append(s)
        return requests.post(url, json={'imgs': imgs})
    except requests.exceptions.RequestException as e:
        print(e)


def send_api_requests(paths):
    try:
        url = 'http://{0}:{1}/method2'.format('localhost', 8080)
        input_path = ""
        output_path = ""
        for (i, o) in paths:
            input_path += i + "|"
            output_path += o + "|"
        return requests.post(url, json={'input_path': input_path, 'output_path': output_path, 'size': str(len(paths))})
    except requests.exceptions.RequestException as e:
        print(e)


def health():
    try:
        url = 'http://{0}:{1}/health'.format('localhost', 8080)
        resp = requests.get(url).json()
        print(resp)
    except requests.exceptions.RequestException as e:
        print(e)


def test_send_api_request():
    for i in range(1, 13):
        health()
        filename = str(i)
        resp = send_api_request("examples/" + filename + ".jpg", "cutouts/cutout_" + filename + ".png").json()


def test_send_api_request_bytes():
    for i in range(1, 2):
        health()
        filename = str(i)
        resp = send_api_request_bytes("examples/" + filename + ".jpg").json()
        inp = io.BytesIO(resp['result'].encode('ISO-8859-1'))
        imageFile = Image.open(inp)
        imageFile.show()


def test_send_api_requests():
    request = []
    health()
    for i in range(1, 3):
        filename = str(i)
        request.append(("examples/" + filename + ".jpg", "cutouts/cutout_" + filename + ".png"))
    resp = send_api_requests(request)
    print(resp)


def test_send_api_requests_bytes():
    request = []
    health()
    for i in range(1, 3):
        filename = str(i)
        request.append("examples/" + filename + ".jpg")
    resp = send_api_requests_bytes(request)
    print(resp)


if __name__ == '__main__':
    test_send_api_requests_bytes()
