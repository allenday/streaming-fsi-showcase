import os
import base64
import json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

# Use the application default credentials
cred = credentials.ApplicationDefault()
firebase_admin.initialize_app(cred, {
  'projectId': os.environ.get('GCP_PROJECT'),
})

db = firestore.client()

def write_candlestick_to_firestore(request):
    request_json = request.get_json()
    if 'subscription' not in request_json or 'message' not in request_json:
        return abort(400)

    subscription = request_json['subscription']
    collection = subscription.split('/')[-1]

    data = request_json['message']['data']
    data_bytes = data.encode('ascii')
    message_bytes = base64.b64decode(data_bytes)
    message = message_bytes.decode('utf-8')
    candlestick = json.loads(message)

    doc_ref = db.collection(collection).document(u't_{}'.format(candlestick['timestamp']))
    doc_ref.set({
        u'candlestick': {
            u'open': candlestick['open'],
            u'close': candlestick['close'],
            u'low': candlestick['low'],
            u'high': candlestick['high']
        },
        u'timestamp': candlestick['timestamp']
    })

    return 'OK'
