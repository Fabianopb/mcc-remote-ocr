import tornado.ioloop
import tornado.web
from pymongo import ReturnDocument
from tornado.web import RequestHandler
from tornado import gen
from pymongo import MongoClient
from bson.objectid import ObjectId
from bson.errors import InvalidId
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
        username = self.get_body_argument('username')
        password = self.get_body_argument('password')
        hashed_password = hashlib.sha256(password.encode('utf-8')).hexdigest()
        user = {
            'username': username,
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
    """
    Registers a new transaction in the database, which is used for monitoring image uploads.
    """

    def post(self):
        # TODO: Implement authentication
        username = 'test'

        images_total = int(self.get_body_argument('images_total'))
        result = db.transactions.insert_one({
            'username': username,
            'creation_time': datetime.utcnow(),
            'images_total': images_total,
            'seq': 0,
            'ocr': [],
            'image_fs_ids': []
        })
        response = generate_json_message('Ready for upload', str(result.inserted_id), 1)
        self.write(response)


class UploadImageHandler(RequestHandler):
    """
    Handles individual image uploads and updates the user's records, when last image of a transaction is uploaded.
    """

    @gen.coroutine
    def post(self):
        # TODO: Implement authentication
        username = 'test'

        # Check that UID is a valid ObjectId
        try:
            uid = ObjectId(self.get_body_argument('uid'))
        except InvalidId:
            self.set_status(400)
            response = generate_json_message('Malformed UID', self.get_body_argument('uid'), 0)
            self.finish(response)
            return

        seq = int(self.get_body_argument('seq'))

        # Check that transaction exists in database
        transaction = db.transactions.find_one({'_id': uid})
        if transaction is None:
            self.set_status(400)
            response = generate_json_message('No such UID in database', uid, 0)
            self.finish(response)
            return

        # Check that 'image' form element is in the request
        try:
            image = self.request.files['image'][0]
        except LookupError:
            self.set_status(400)
            response = generate_json_message('No image in request', uid, get_next_seq(uid))
            self.finish(response)
            return

        # Check that the uploaded image has the expected seq number
        if seq != get_next_seq(uid):
            self.set_status(400)
            response = generate_json_message('Incorrect upload order', uid, get_next_seq(uid))
            self.finish(response)
            return

        # Perform OCR and store image and its thumbnail in GridFS
        ocr_text, image_fs_id, thumbnail_fs_id = yield perform_ocr_and_store(image)
        image_fs_ids = {
            'image_fs_id': image_fs_id,
            'thumbnail_fs_id': thumbnail_fs_id
        }

        # Update transaction document in DB
        transaction = db.transactions.find_one_and_update({'_id': uid}, {'$set': {'seq': seq}, '$push': {
            'ocr': ocr_text,
            'image_fs_ids': image_fs_ids
        }}, return_document=ReturnDocument.AFTER)

        # If this was the last expected image, do final processing
        if seq == transaction['images_total']:
            ocr_result = '\n'.join(transaction['ocr'])
            record = {
                'creation_time': datetime.utcnow(),
                'image_fs_ids': transaction['image_fs_ids'],
                'ocr_text': ocr_result
            }
            print(record)
            # TODO: Error handling and cleanup if database update fails
            # Update user document in DB
            db.users.update_one({"username": username}, {'$push': {'records': record}})
            # Delete transaction document from DB
            db.transactions.delete_one({'_id': uid})

            # Respond with the final combined text from OCR
            response = generate_json_message("OCR finished", uid, 0, ocr_result=ocr_result)
            self.write(response)
        else:
            # If more images are expected, respond with the expected seq number
            response = generate_json_message('Image processed', uid, seq + 1)
            print(response)
            self.write(response)


@gen.coroutine
def perform_ocr_and_store(image):
    """
    Performs Tesseract OCR processing on the given image, creates a thumbnail and stores the image and its
    thumbnail to the database using GridFS.

    Returns OCR results and Object ID:s to the GridFS files.

    :param image: Image to process as a Tornado File from a multipart/form-data request.
    :return: OCR result text, original image's ID in GridFS, thumbnail's ID in GridFS
    """
    print('Processing ' + image['filename'])
    pil_image = Image.open(BytesIO(image['body']))
    ocr_text = pytesseract.image_to_string(pil_image)
    thumbnail = create_thumbnail(pil_image)
    image_fs_id = fs.put(image['body'], content_type=image['content_type'], filename=image['filename'])
    thumbnail_fs_id = fs.put(thumbnail, content_type='image/jpeg', filename='t_' + image['filename'])
    return ocr_text, image_fs_id, thumbnail_fs_id


def create_thumbnail(image):
    """
    Creates a thumbnail of the PIL image given as a parameter, and returns it as a byte array in JPEG format.

    :param image: PIL image for thumbnail creation
    :return: Thumbnail as a byte array in JPEG format
    """
    image.thumbnail(THUMBNAIL_SIZE)
    thumbnail_bytes = BytesIO()
    image.save(thumbnail_bytes, format='JPEG')
    return thumbnail_bytes.getvalue()


def get_next_seq(transaction_id):
    """
    Returns the sequence number of the next expected image for a transaction.

    :param transaction_id: The transaction document's ObjectID
    :return: Next expected seq number
    """
    transaction = db.transactions.find_one({'_id': transaction_id})
    return transaction['seq'] + 1


def generate_json_message(message, transaction_id, next_seq, ocr_result=''):
    """
    Generates a JSON formatted string for responses.

    :param message:
    :param transaction_id:
    :param next_seq:
    :param ocr_result:
    :return:
    """
    msg = {
        'uid': str(transaction_id),
        'message': message,
        'next_seq': next_seq,
        'ocr_result': ocr_result
    }
    return msg


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
        (r'/upload/', UploadImageHandler),
    ])


# Main app
if __name__ == '__main__':
    app = make_app()

    client = MongoClient('mongodb://mongo:27017')
    if client is None:
        print("Connection to database failed")
    db = client.userdata
    fs = GridFS(db)
    app.listen(80)
    tornado.ioloop.IOLoop.current().start()
