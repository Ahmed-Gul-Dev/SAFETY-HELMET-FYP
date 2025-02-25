import cv2
import numpy
import platform
import requests
import serial
import imutils
import firebase_admin
import tkinter

print("Opencv based Object Detection")
fontscale = 2
font = cv2.FONT_HERSHEY_PLAIN
config_file = 'ssd_mobilenet_v3_large_coco_2020_01_14.pbtxt'
frozen_model = 'frozen_inference_graph.pb'

model = cv2.dnn_DetectionModel(frozen_model,config_file)

labels = []
filename = 'coco.txt'
with open(filename,'rt') as fpt:
    labels = fpt.read().rstrip('\n').split('\n')

# print(labels)
# print(len(labels))

# Model Configuration
model.setInputSize(320,320)
model.setInputScale((1.0/127.5)) #255/2 = 127.5
model.setInputMean((127.5,127.5,127.5))
model.setInputSwapRB(True)

# define a video capture object
# cap = cv2.VideoCapture(0)
# 
# while True:
#     _, frame = cap.read()
#     Classindex , confidence , bbox = model.detect(frame,confThreshold=0.5)
#     for classind, conf, boxes in zip(Classindex.flatten(), confidence.flatten(), bbox):
#         if classind < 2:
#             print(labels[classind - 1])
#             cv2.rectangle(frame,boxes,(0,255,0),2) # frame,(x,y),(x+w,y+h),color,thickness
# 
#     cv2.imshow("Output", frame)
#     if cv2.waitKey(1) & 0xFF == ord('q'):
#         break
# 
# cap.release()
# cv2.destroyAllWindows()

# Read an Image
# img = cv2.imread('1.jpg')
# h, w, _ = img.shape
# print('width: ', w)
# print('height:', h)
# # cv2.imshow('BGR',img)  # BGR
# img = cv2.resize(img , (640,480))
#
# Classindex , confidence , bbox = model.detect(img,confThreshold=0.5)
# print(len(Classindex))
#
# for i in range(0,len(Classindex)):
#     result = labels[Classindex[i] - 1]
#     print(result)
#
#
# for classind,conf,boxes in zip(Classindex.flatten(),confidence.flatten(),bbox):
#     cv2.rectangle(img,boxes,(0,255,0),2) # frame,(x,y),(x+w,y+h),color,thickness
#     # cv2.putText(img,labels[classind-1],(boxes[0]+2,boxes[1]+0),font,fontScale=fontscale,color=(255,0,0),thickness=1)
#
# # img = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
# cv2.imshow('Result',img) # RGB
#
# cv2.waitKey(0)
# cv2.destroyAllWindows()