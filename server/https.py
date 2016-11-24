import tornado.httpserver
import tornado.ioloop
import tornado.web

class getToken(tornado.web.RequestHandler):
    def get(self):
        self.write("Welcome to backend")

application = tornado.web.Application([
    (r'/', getToken),
])

if __name__ == '__main__':
    http_server = tornado.httpserver.HTTPServer(application, ssl_options={
        "certfile": "/home/mborekcz/Dropbox/Projects/mobile_clouds2/cert/nopass_cert.pem",
        "keyfile": "/home/mborekcz/Dropbox/Projects/mobile_clouds2/cert/nopass_key.pem",
    })
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()
