# import the opencv library
import cv2

# define a video capture object
cap = cv2.VideoCapture(0)

while True:
    _, frame = cap.read()
    frame = cv2.resize(frame,(320,320))

#    cv2.imshow("Output", frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
