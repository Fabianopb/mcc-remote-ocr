from pymongo.errors import AutoReconnect
from pymongo.database import Database
from pymongo.collection import Collection
from pymongo import ReturnDocument
from tornado import gen
import logging

"""
Fail-over tolerant MongoDB database operations.

Each operation is retried 5 times with increasing delays between tries.
"""


# Decorator which runs a function again 5 times when an  AutoReconnect exception is thrown
def retry_on_autoreconnect(f):
    @gen.coroutine
    def f_retry(*args, **kwargs):
        for i in range(5):
            try:
                return f(*args, **kwargs)
            except AutoReconnect:
                yield gen.sleep(i + 1)
                continue
        logging.error('Database fail-over timed out after 5 reconnect attempts, operation failed.')

    return f_retry


@retry_on_autoreconnect
def find_user(db_object, username):
    if isinstance(db_object, Database):
        return db_object.users.find_one({'username': username})
    elif isinstance(db_object, Collection):
        return db_object.find_one({'username': username})
    else:
        raise TypeError('db_object must be either Database or Collection')


@retry_on_autoreconnect
def insert_user(db_object, user):
    if isinstance(db_object, Database):
        return db_object.users.insert_one(user)
    elif isinstance(db_object, Collection):
        return db_object.insert_one(user)
    else:
        raise TypeError('db_object must be either Database or Collection')


@retry_on_autoreconnect
def update_user(db_object, username, update):
    if isinstance(db_object, Database):
        return db_object.users.update_one({'username': username}, update)
    elif isinstance(db_object, Collection):
        return db_object.update_one({'username': username}, update)
    else:
        raise TypeError('db_object must be either Database or Collection')


@retry_on_autoreconnect
def find_transaction(db_object, transaction_id):
    if isinstance(db_object, Database):
        return db_object.transactions.find_one({'_id': transaction_id})
    elif isinstance(db_object, Collection):
        return db_object.find_one({'_id': transaction_id})
    else:
        raise TypeError('db_object must be either Database or Collection')


@retry_on_autoreconnect
def insert_transaction(db_object, transaction):
    if isinstance(db_object, Database):
        return db_object.transactions.insert_one(transaction)
    elif isinstance(db_object, Collection):
        return db_object.insert_one(transaction)
    else:
        raise TypeError('db_object must be either Database or Collection')


@retry_on_autoreconnect
def find_and_update_transaction(db_object, transaction_id, update):
    if isinstance(db_object, Database):
        return db_object.transactions.find_one_and_update({'_id': transaction_id}, update,
                                                          return_document=ReturnDocument.AFTER)
    elif isinstance(db_object, Collection):
        return db_object.find_one_and_update({'_id': transaction_id}, update, return_document=ReturnDocument.AFTER)
    else:
        raise TypeError('db_object must be either Database or Collection')


@retry_on_autoreconnect
def delete_transaction(db_object, transaction_id):
    if isinstance(db_object, Database):
        return db_object.transactions.delete_one({'_id': transaction_id})
    elif isinstance(db_object, Collection):
        return db_object.delete_one(transaction_id)
    else:
        raise TypeError('db_object must be either Database or Collection')
