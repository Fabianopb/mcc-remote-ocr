import tornado.ioloop
import tornado.web
from tornado.web import RequestHandler
from pymongo import MongoClient
import hashlib
from PIL import Image
import pytesseract
from io import BytesIO


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


class OCRHandler(RequestHandler):
    # TODO: Change to self.current_user after authentication is done
    user = 'test'

    def post(self):
        # Check that 'images' form element is in the request
        try:
            image_files = self.request.files['images']
        except KeyError:
            self.set_status(400)
            self.finish('Missing \"images\" argument')
            return

        # Check that at least one image was uploaded
        if len(image_files) == 0:
            self.set_status(400)
            self.finish('No images uploaded')
            return

        # TODO: Change code to do async processing
        ocr_data = []
        for image in image_files:
            print(image['filename'])
            ocr_data.append(pytesseract.image_to_string(Image.open(BytesIO(image['body']))))
        result = ' '.join(ocr_data)
        self.write(result)


# URL routes etc.
def make_app():
    return tornado.web.Application([
        (r'/', MainHandler),
        (r'/db/', DbTestHandler),
        (r'/add_user/', AddUserHandler),
        (r'/test_ocr/', OCRTestHandler),
        (r'/ocr/', OCRHandler),
    ])

if __name__ == '__main__':
    app = make_app()

    client = MongoClient('mongodb://mongo:27017')
    db = client.userdata
    app.listen(80)
    tornado.ioloop.IOLoop.current().start()
