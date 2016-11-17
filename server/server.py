import tornado.ioloop
import tornado.web
from tornado.web import RequestHandler
from pymongo import MongoClient
import hashlib
from PIL import Image
import pytesseract


class MainHandler(RequestHandler):
    def get(self):
        self.write('Backend is running!')


# Test function which displays MongoDB info
class DbTestHandler(RequestHandler):
    def get(self):
        self.write(client.server_info())


# Test function for adding users to the DB
class AddUserHandler(RequestHandler):
    def post(self):
        user = self.get_body_argument('user')
        password = self.get_body_argument('password')
        hashed_password = hashlib.sha256(password.encode('utf-8')).hexdigest()
        user = {
            'user_name': user,
            'password': hashed_password,
            'records': []
        }
        db.users.insert_one(user)

        self.write('OK')


# Test function for running OCR on test.jpg
class OCRTestHandler(RequestHandler):
    def get(self):
        self.write(pytesseract.image_to_string(Image.open('test.jpg')))


# URL routes etc.
def make_app():
    return tornado.web.Application([
        (r'/', MainHandler),
        (r'/db/', DbTestHandler),
        (r'/add_user/', AddUserHandler),
        (r'/test_ocr/', OCRTestHandler),
    ])

if __name__ == '__main__':
    app = make_app()

    client = MongoClient('mongodb://mongo:27017')
    db = client.userdata
    app.listen(80)
    tornado.ioloop.IOLoop.current().start()
