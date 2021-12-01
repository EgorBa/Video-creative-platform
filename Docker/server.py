import json
import os
import time
import cherrypy
from cherrypy import log
import yaml
import cv2
import numpy as np
import io
from rembg.bg import remove
from PIL import Image
import psutil
import torch
import torch.nn as nn
import torch.nn.functional as F
import torchvision.transforms as transforms

from Docker.MODNet.src.models.modnet import MODNet

process = psutil.Process(os.getpid())  # for monitoring and debugging purposes

config = yaml.safe_load(open("config.yml"))


def process_api_request(body):
    """
    This methos is for extracting json parameters and processing the actual api request.
    All api format and logic is to be kept in here.
    :param body:
    :return:
    """
    input_path = body.get('input_path')
    output_path = body.get('output_path')

    result_str = 'image has been processed'
    start = time.time()
    try:
        if contains_people(input_path):
            use_modnet(input_path, output_path)
            pass
        else:
            if contains_white_bg(input_path):
                use_cv(input_path, output_path)
            else:
                use_rembg(input_path, output_path)
    except Exception:
        result_str = 'Something went wrong'

    time_spent = time.time() - start
    log("Completed api call.Time spent {0:.3f} s".format(time_spent))

    result = {
        'result': result_str
    }
    return json.dumps(result)


def use_cv(input_path, output_path):
    img = cv2.imread(input_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    mask = cv2.threshold(gray, 250, 255, cv2.THRESH_BINARY)[1]
    mask = 255 - mask
    kernel = np.ones((3, 3), np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
    mask = cv2.GaussianBlur(mask, (0, 0), sigmaX=2, sigmaY=2, borderType=cv2.BORDER_DEFAULT)
    mask = (2 * (mask.astype(np.float32)) - 255.0).clip(0, 255).astype(np.uint8)
    result = img.copy()
    result = cv2.cvtColor(result, cv2.COLOR_BGR2BGRA)
    result[:, :, 3] = mask
    cv2.imwrite(output_path, result)


def use_rembg(input_path, output_path):
    f = np.fromfile(input_path)
    result = remove(f)
    img = Image.open(io.BytesIO(result)).convert("RGBA")
    img.save(output_path)

def use_modnet(input_path, output_path):
    ckpt_path = 'modnet_photographic_portrait_matting.ckpt'
    ref_size = 512
    im_transform = transforms.Compose(
        [
            transforms.ToTensor(),
            transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
        ]
    )
    modnet = MODNet(backbone_pretrained=False)
    modnet = nn.DataParallel(modnet).cuda()
    modnet.load_state_dict(torch.load(ckpt_path))
    modnet.eval()
    im = Image.open(input_path)
    im = np.asarray(im)
    if len(im.shape) == 2:
        im = im[:, :, None]
    if im.shape[2] == 1:
        im = np.repeat(im, 3, axis=2)
    elif im.shape[2] == 4:
        im = im[:, :, 0:3]
    im = Image.fromarray(im)
    im = im_transform(im)
    im = im[None, :, :, :]
    im_b, im_c, im_h, im_w = im.shape
    if max(im_h, im_w) < ref_size or min(im_h, im_w) > ref_size:
        if im_w >= im_h:
            im_rh = ref_size
            im_rw = int(im_w / im_h * ref_size)
        elif im_w < im_h:
            im_rw = ref_size
            im_rh = int(im_h / im_w * ref_size)
    else:
        im_rh = im_h
        im_rw = im_w
    im_rw = im_rw - im_rw % 32
    im_rh = im_rh - im_rh % 32
    im = F.interpolate(im, size=(im_rh, im_rw), mode='area')
    _, _, matte = modnet(im.cuda(), True)
    matte = F.interpolate(matte, size=(im_h, im_w), mode='area')
    matte = matte[0][0].data.cpu().numpy()
    Image.fromarray(((matte * 255).astype('uint8')), mode='L').save(output_path)

def contains_people(path):
    # TODO use tourchvision
    return False


def contains_white_bg(path):
    image = Image.open(path)
    pix = image.load()
    a = pix[0, 0][0]
    b = pix[0, 0][1]
    c = pix[0, 0][2]
    if a + b + c > 750:
        return True
    return False


class ApiServerController(object):
    @cherrypy.expose('/health')
    def health(self):
        result = {
            "status": "OK",  # TODO when is status not ok?
            "info": {
                "mem": "{0:.3f} MiB".format(process.memory_info().rss / 1024.0 / 1024.0),
                "cpu": process.cpu_percent(),
                "threads": len(process.threads())
            }
        }
        return json.dumps(result).encode("utf-8")

    @cherrypy.expose('/method1')
    def method1(self):
        cl = cherrypy.request.headers['Content-Length']
        raw = cherrypy.request.body.read(int(cl))
        body = json.loads(raw)
        return process_api_request(body).encode("utf-8")


if __name__ == '__main__':
    cherrypy.tree.mount(ApiServerController(), '/')

    cherrypy.config.update({
        'server.socket_port': config["app"]["port"],
        'server.socket_host': config["app"]["host"],
        'server.thread_pool': config["app"]["thread_pool"],
        'log.access_file': "access1.log",
        'log.error_file': "error1.log",
        'log.screen': True,
        'tools.response_headers.on': True,
        'tools.encode.encoding': 'utf-8',
        'tools.response_headers.headers': [('Content-Type', 'application/json;encoding=utf-8')],
    })

    try:
        cherrypy.engine.start()
        cherrypy.engine.block()
    except KeyboardInterrupt:
        cherrypy.engine.stop()
