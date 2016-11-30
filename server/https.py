import tornado.httpserver
import tornado.ioloop
import tornado.web


'''
class getToken(tornado.web.RequestHandler):
    def get(self):
        self.write("Welcome to backend")

application = tornado.web.Application([
    (r'/', getToken),
])
'''


class MainHandler(tornado.web.RequestHandler):
    @tornado.web.authenticated
    def get(self):
        self.write('Backend is running!')

class LoginHandler(tornado.web.RequestHandler):
    def get(self):
        self.write('Authentication')


class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r"/", MainHandler),
            (r"/login/", LoginHandler),
        ]
        settings = {
            "cookie_secret": "our_group_secret_posdf3fo",
            "login_url": "login/"
        }
        tornado.web.Application.__init__(self, handlers, **settings)


'''
# URL routes etc.
def make_app():
    return tornado.web.Application(handlers=[
        (r'/', MainHandler),
        (r'/login/', LoginHandler),
        #(r'/db/', DbTestHandler),
        #(r'/add_user/', AddUserHandler),
        #(r'/test_ocr/', OCRTestHandler),
    ],settings={
        "login_url": "/login/"
    })
'''

if __name__ == '__main__':
    app = Application()

    http_server = tornado.httpserver.HTTPServer(app, ssl_options={
        "certfile": "cert/nopass_cert.pem",
        "keyfile": "cert/nopass_key.pem",
    })
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()
