import os
import sys
import argparse
import numpy as np
from PIL import Image
from pymatting import cutout
from random import randrange
from threading import Thread
import torch
import torch.nn as nn
import torch.nn.functional as F
import torchvision.transforms as transforms

from MODNet.src.models.modnet import MODNet

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

if __name__ == '__main__':
    for i in range(1, 13):
        filename = str(i)
        input_path = "examples/" + filename + ".jpg"
        output_path = "cutouts/cutout_" + filename + ".png"
        ckpt_path = 'modnet_photographic_portrait_matting.ckpt'
        ref_size = 512
        im_transform = transforms.Compose(
            [
                transforms.ToTensor(),
                transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
            ]
        )
        modnet = MODNet(backbone_pretrained=False)
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        modnet = nn.DataParallel(modnet).to(device)
        modnet.load_state_dict(torch.load(ckpt_path, map_location=device))
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
        _, _, matte = modnet(im.to(device), True)
        matte = F.interpolate(matte, size=(im_h, im_w), mode='area')
        matte_path = str(randrange(10)) + ".png"
        matte = matte[0][0].data.cpu().numpy()
        Image.fromarray(((matte * 255).astype('uint8')), mode='L').save(matte_path)
        cutout(input_path, matte_path, output_path)
        th = Thread(target=os.remove, args=(matte_path,))
        th.start()
