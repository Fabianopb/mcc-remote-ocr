import tornado.ioloop
import tornado.web
import tornado.httpserver
from pymongo import ReturnDocument
from tornado.web import RequestHandler
from tornado import gen
from pymongo import MongoClient
from bson.objectid import ObjectId
from bson.errors import InvalidId
from bson.codec_options import CodecOptions
import hashlib
from PIL import Image
import pytesseract
from io import BytesIO
from gridfs import GridFS
import datetime
import tornado.options
import logging
from pytz import timezone
import functools
import base64
import json
from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)
import requests
import pprint

THUMBNAIL_SIZE = 128, 128
SOURCE_IMAGE_LIFETIME = 7

SECRET_KEY = '5$4asRfg_thisAppIsAwesome:)'
TOKEN_EXPIRATION = 3600  # 60 minutes

APP_FB_TOKEN = '349946252046985|lJ9EY8Rs_63dP6I7ei0liQlEybQ'
FB_SUFFIX = "@facebook.com"
FB_NO_PASS = "FB account"

def verify_password(userToken, password):
    user = verify_auth_token(userToken)
    if user:  # User from token
        return True
    else:
        if userToken.endswith(FB_SUFFIX): #FB account; cannot be authorised this way
            return False
        userEntry = db.users.find_one({"user_name": userToken})
        if userEntry is not None:
            if hashlib.sha256(password.encode('utf-8')).hexdigest() == userEntry.get('password'):
                user = userEntry.get('user_name')
                return True

    return False


def generate_auth_token(user, expiration=TOKEN_EXPIRATION):
    s = Serializer(SECRET_KEY, expires_in=expiration)
    return s.dumps({'id': user})


def verify_auth_token(token):
    s = Serializer(SECRET_KEY)
    try:
        data = s.loads(token)
    except SignatureExpired:
        return None  # valid token, but expired
    except BadSignature:
        return None  # invalid token

    if db.users.find_one({"user_name": data['id']}) is not None:
        return data['id']
    else:
        return None


def requireAuthentication(auth):
    def applyAuthentication(func):
        def _authenticate(requestHandler):
            requestHandler.set_status(401)
            requestHandler.set_header('WWW-Authenticate', 'Basic realm=temerariousRealm')
            requestHandler.finish()
            return False

        @functools.wraps(func)
        def newFunc(*args):
            handler = args[0]

            authHeader = handler.request.headers.get('Authorization')
            if authHeader is None:
                return _authenticate(handler)
            if authHeader[:6] != 'Basic ':
                return _authenticate(handler)

            authDecoded = base64.b64decode(authHeader[6:])
            userToken, password = authDecoded.decode().split(':', 2)

            if (auth(userToken, password)):
                func(*args, username=userToken)
            else:
                _authenticate(handler)

        return newFunc

    return applyAuthentication


class TokenHandler(tornado.web.RequestHandler):
    @requireAuthentication(verify_password)
    def get(self, username):
        print('Token for user: ' + str(username))

        token = generate_auth_token(username)
        print(json.dumps({'token': token.decode('ascii')}))

        self.write(json.dumps({'token': token.decode('ascii')}))

class FBTokenHandler(tornado.web.RequestHandler):
    def get(self):
        userToken = self.get_argument('token')

        # Verify userToken by FB
        r = requests.get('https://graph.facebook.com/debug_token?input_token='
                + userToken + '&' + 'access_token=' + APP_FB_TOKEN)
        
        #if r.status_code != requests.codes.ok:
        if r.status_code != 200:
            respond_and_log_error(self, 401, 'Authentication failed')
            return
        receivedData = json.loads(r.text).get('data')

        if receivedData.get('error') is not None:
            respond_and_log_error(self, 401, receivedData.get('error').get('message'))
            return

        pprint.pprint(json.loads(r.receivedData))

        # Extract user id, use it as a username, concatenated with FB_SUFFIX,
        # so that it does not interfere with locally registered users
        userId = receivedData.get('user_id')
        username = userId + FB_SUFFIX

        # FB user still needs to be in the local database. Check if this account
        # is already there; if not, add it.
        if db.users.find_one({"user_name": username}) is None:
            user = {
                'user_name': user,
                'password': FB_NO_PASS, # FB users cannot be authorised locally
                'records': []
            }
            db.users.insert_one(user)

        token = generate_auth_token(username)

        self.write(json.dumps({'token': token.decode('ascii')}))


class OtherHandler(tornado.web.RequestHandler):
    @requireAuthentication(verify_password)
    def get(self, username):
        self.write('You are authorised')


class MainHandler(RequestHandler):
    def get(self):
        self.write('Backend is running!')


# Test function which displays MongoDB info
class DbTestHandler(RequestHandler):
    def get(self):
        logging.debug(self.request)
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


class AddTestuserHandler(RequestHandler):
    def get(self):
        user = 'testuser'
        password = 'time2work'
        hashed_password = hashlib.sha256(password.encode('utf-8')).hexdigest()
        user = {
            'user_name': user,
            'password': hashed_password,
            'records': []
        }
        db.users.insert_one(user)
        self.write('OK')


class GetTestuserHandler(RequestHandler):
    def get(self):
        # user = db.users.find_one({"user_name": 'testuser'})
        user = db.users.find_one({"user_name": 'testuser'})
        self.write(user.get[user_name])


class GetRecordsHandler(RequestHandler):
    """
    Gets a given amount of OCR records from the database and returns the data as JSON
    """

    @gen.coroutine
    def get(self):
        logging.debug(self.request)

        # TODO: Implement authentication
        username = 'test'

        amount = int(self.get_query_argument('amount'))

        # Get timezone-aware users-collection
        users_tz = db.users.with_options(codec_options=CodecOptions(
            tz_aware=True,
            tzinfo=timezone('Europe/Helsinki')))

        user = users_tz.find_one({'username': username})
        records = {'records': user['records'][-amount:]}

        self.write(json.dumps(records, cls=JSONDateTimeEncoder))


class GetImageHandler(RequestHandler):
    """
    Gets an image from the GridFS database.
    """

    @gen.coroutine
    def get(self, slug):
        logging.debug(self.request)

        # Check that slug is a valid ObjectId
        try:
            fs_id = ObjectId(slug)
        except InvalidId:
            respond_and_log_error(self, 404, 'Malformed image ID')
            return

        # Check that image exists in GridFS
        image = fs.find_one({'_id': fs_id})
        if image is None:
            respond_and_log_error(self, 404, 'No such image in database')
            return

        self.set_header("Content-type", image.content_type)
        self.write(image.read())


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

        logging.debug(self.request)

        images_total = int(self.get_body_argument('images_total'))
        result = db.transactions.insert_one({
            'username': username,
            'creation_time': datetime.datetime.utcnow(),
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
        logging.debug(self.request)
        # Check that UID is a valid ObjectId
        try:
            uid = ObjectId(self.get_body_argument('uid'))
        except InvalidId:
            response = generate_json_message('Malformed UID', self.get_body_argument('uid'), 0)
            respond_and_log_error(self, 400, response)
            return

        seq = int(self.get_body_argument('seq'))

        # Check that transaction exists in database
        transaction = db.transactions.find_one({'_id': uid})
        if transaction is None:
            response = generate_json_message('No such UID in database', uid, 0)
            respond_and_log_error(self, 400, response)
            return

        # Check that 'image' form element is in the request
        try:
            image = self.request.files['image'][0]
        except LookupError:
            response = generate_json_message('No image in request', uid, get_next_seq(uid))
            respond_and_log_error(self, 400, response)
            return

        # Check that the uploaded image has the expected seq number
        if seq != get_next_seq(uid):
            response = generate_json_message('Incorrect upload order', uid, get_next_seq(uid))
            respond_and_log_error(self, 400, response)
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
                'creation_time': datetime.datetime.utcnow(),
                'image_fs_ids': transaction['image_fs_ids'],
                'ocr_text': ocr_result
            }
            logging.debug(record)
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
            logging.debug(response)
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
    logging.debug('Processing ' + image['filename'])
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


# Logs and responds with the given error code and message
def respond_and_log_error(request, error_code, msg):
    request.set_status(error_code)
    logging.debug("Error response: " + str(msg))
    request.finish(msg)
    return


# JSONEncoder, which outputs date and datetime in ISO-format and ObjectID as hex string
class JSONDateTimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (datetime.date, datetime.datetime)):
            return obj.isoformat()
        elif isinstance(obj, (ObjectId)):
            return str(obj)
        else:
            return json.JSONEncoder.default(self, obj)


# URL routes etc.
def make_app():
    return tornado.web.Application([
        (r'/', MainHandler),
        (r'/token', TokenHandler),
        (r'/fb_token', FBTokenHandler),
        (r'/other', OtherHandler),
        (r'/db/', DbTestHandler),
        (r'/add_user/', AddUserHandler),
        (r'/get_testuser/', AddTestuserHandler),
        (r'/add_testuser/', AddTestuserHandler),
        (r'/test_ocr/', OCRTestHandler),
        (r'/ocr/', OCRHandler),
        (r'/upload/', UploadImageHandler),
        (r'/records/', GetRecordsHandler),
        (r'/image/([^/]+)', GetImageHandler),
    ])


# Main app
if __name__ == '__main__':
    app = make_app()

    # Set up logging
    tornado.options.parse_command_line()

    client = MongoClient('mongodb://mongo:27017')
    if client is None:
        logging.debug("Connection to database failed")
    db = client.userdata
    fs = GridFS(db)

    http_server = tornado.httpserver.HTTPServer(app)
    http_server.listen(80)

    https_server = tornado.httpserver.HTTPServer(app, ssl_options={
        "certfile": "cert/nopass_cert.pem",
        "keyfile": "cert/nopass_key.pem",
    })
    https_server.listen(443)
    tornado.ioloop.IOLoop.instance().start()
