import tornado.ioloop
import tornado.web
from tornado.web import RequestHandler
from pymongo import MongoClient
import hashlib
from PIL import Image
import pytesseract
from io import BytesIO
from gridfs import GridFS
from datetime import datetime


THUMBNAIL_SIZE = 128, 128
SOURCE_IMAGE_LIFETIME = 7


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
            'username': user,
            'password': hashed_password,
            'records': []
        }
        db.users.insert_one(user)

        self.write('OK')


# Test function for running OCR on test.jpg
class OCRTestHandler(RequestHandler):
    def get(self):
        self.write(pytesseract.image_to_string(Image.open('test.jpg')))


# Initial handler for OCR image processing
class OCRHandler(RequestHandler):

    def post(self):
        # TODO: Change to self.current_user after authentication is done
        username = 'test'

        # Check that 'images' form element is in the request
        try:
            image_files = self.request.files['images']
        except KeyError:
            self.set_status(400)
            self.finish('Missing \"images\" argument')
            return

        image_count = len(image_files)
        # Check that at least one image was uploaded
        if image_count == 0:
            self.set_status(400)
            self.finish('No images uploaded')
            return

        # TODO: Change code to do async processing
        print('Received ' + str(image_count) + ' images')
        ocr_data = []
        image_fs_id_array = []
        fs = GridFS(db)

        for image in image_files:
            print('Processing ' + image['filename'])
            pil_image = Image.open(BytesIO(image['body']))
            ocr_data.append(pytesseract.image_to_string(pil_image))
            thumbnail = create_thumbnail(pil_image)
            image_fs_id = (fs.put(image['body'], content_type=image['content_type'], filename=image['filename']))
            thumbnail_fs_id = (fs.put(thumbnail, content_type='image/jpeg', filename='t_' + image['filename']))
            image_fs_id_array.append({'image_fs_id': image_fs_id, 'thumbnail_fs_id': thumbnail_fs_id})

        ocr_result = '\n'.join(ocr_data)

        ocr_record = {
            'creation_time': datetime.utcnow(),
            'image_fs_id_array': image_fs_id_array,
            'ocr_text': ocr_result
        }

        print(ocr_record)

        # TODO: Error handling and cleanup if database update fails
        db.users.update_one({"username": username}, {'$push': {'records': ocr_record}})

        self.write(ocr_result)


def create_thumbnail(image):
    """Create a thumbnail of the image given as a parameter, and returns it as a byte array in JPEG format

    Keyword arguments:
    image -- The image as a PIL Image object
    """
    image.thumbnail(THUMBNAIL_SIZE)
    thumbnail_bytes = BytesIO()
    image.save(thumbnail_bytes, format='JPEG')
    return thumbnail_bytes.getvalue()

# Get image from db
#    filetest = fs.find_one({'filename': 'test.jpg'})
#    self.set_header("Content-type", filetest.content_type)
#    self.write(filetest.read())


# URL routes etc.
def make_app():
    return tornado.web.Application([
        (r'/', MainHandler),
        (r'/db/', DbTestHandler),
        (r'/add_user/', AddUserHandler),
        (r'/test_ocr/', OCRTestHandler),
        (r'/ocr/', OCRHandler),
    ])

# Main app
if __name__ == '__main__':
    app = make_app()

    client = MongoClient('mongodb://mongo:27017')
    db = client.userdata
    app.listen(80)
    tornado.ioloop.IOLoop.current().start()
