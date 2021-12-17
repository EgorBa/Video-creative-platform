import requests
import json

# This test is about sending requests to the server.


def send_api_request(input_path, output_path):
    try:
        url = 'http://{0}:{1}/method1'.format('localhost', 8080)
        return requests.post(url, json={'input_path': input_path, 'output_path': output_path})
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


if __name__ == '__main__':

    request = []
    for i in range(1, 3):
        filename = str(i)
        request.append(("examples/" + filename + ".jpg", "cutouts/cutout_" + filename + ".png"))
    health()
    resp = send_api_requests(request)
    print(resp)
    # for i in range(1, 13):
    #     health()
    #     filename = str(i)
    #     resp = send_api_request("examples/" + filename + ".jpg", "cutouts/cutout_" + filename + ".png").json()
    #     print(resp)
    health()
