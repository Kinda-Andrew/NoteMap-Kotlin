var http = require('http');
var fs = require('fs');
var path = require('path');

var PORT = process.env.PORT || 8000;
var DATA_FOLDER = path.join(__dirname, 'data');
var MAX_BODY_LENGTH = 2 * 1024 * 1024;
var ALLOWED_KEYS = ['routes', 'status', 'caretakerNotes'];


var FILES = {
  '/': ['index.html', 'text/html'],
  '/app.js': ['app.js', 'text/javascript'],
  '/style.css': ['style.css', 'text/css'],
  '/helper.js': ['helper.js', 'text/javascript']
};

if (!fs.existsSync(DATA_FOLDER)) fs.mkdirSync(DATA_FOLDER);


var server = http.createServer(function (request, response) {
  var url = new URL(request.url, 'http://localhost');
  var file = FILES[url.pathname];

  if (file) return sendFile(response, file[0], file[1]);
  if (url.pathname === '/data') return handleData(request, response, url);

  sendJson(response, 404, { error: 'Not found' });
});

function sendFile(response, fileName, type) {
  response.writeHead(200, { 'Content-Type': type, 'Cache-Control': 'no-cache' });
  response.end(fs.readFileSync(path.join(__dirname, fileName)));
}

function sendJson(response, status, object) {
  response.writeHead(status, { 'Content-Type': 'application/json' });
  response.end(JSON.stringify(object));
}

function readFile(file) {
  if (!fs.existsSync(file)) return {};

  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (error) {
    return {};
  }
}

function handleData(request, response, url) {
  var code = url.searchParams.get('code') || '';
  if (!/^[a-z0-9]{6,40}$/.test(code)) return sendJson(response, 400, { error: 'bad code' });

  var file = path.join(DATA_FOLDER, code + '.json');

  if (request.method === 'GET') return sendJson(response, 200, readFile(file));
  if (request.method !== 'POST') return sendJson(response, 405, { error: 'bad method' });

  var body = '';
  var tooBig = false;

  request.on('data', function (piece) {
    if (body.length + piece.length > MAX_BODY_LENGTH) tooBig = true;
    else body += piece;
  });

  request.on('end', function () {
    if (tooBig) return sendJson(response, 413, { error: 'too big' });

    var message;
    try {
      message = JSON.parse(body);
    } catch (error) {
      return sendJson(response, 400, { error: 'bad json' });
    }

    if (!message || ALLOWED_KEYS.indexOf(message.key) === -1) {
      return sendJson(response, 400, { error: 'bad key' });
    }

    var saved = readFile(file);
    saved[message.key] = message.value;
    fs.writeFileSync(file, JSON.stringify(saved));

    sendJson(response, 200, { ok: true });
  });
}

// open local host on your browser, i got rid of the link you can open on the ide cause it was better on the brower
server.listen(PORT, function () {
  console.log('Running on port ' + PORT);
});
