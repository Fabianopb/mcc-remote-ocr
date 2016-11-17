import tornado.ioloop
import tornado.web
from tornado.web import RequestHandler
from pymongo import MongoClient


class MainHandler(RequestHandler):
    def get(self):
        self.write("Backend is running!")


class DbTestHandler(RequestHandler):
    def get(self):
        self.write(client.server_info())


def make_app():
    return tornado.web.Application([
        (r"/", MainHandler),
        (r"/db/", DbTestHandler),
    ])

if __name__ == "__main__":
    app = make_app()
    client = MongoClient("mongodb://mongo:27017")
    app.listen(80)
    tornado.ioloop.IOLoop.current().start()
