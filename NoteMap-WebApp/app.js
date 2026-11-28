//constants

var OFF_ROUTE_METRES = 60;
var ARRIVED_METRES = 30;
var IDLE_SECONDS = 0.5 * 60; // 0.5 is only for demo and testing, real version would be on 15 as to be at 15mins as stated in our document
var IDLE_RADIUS_METRES = 25;
var CHECK_EVERY_MS = 10000;
var REFRESH_EVERY_MS = 5000;
var MAX_SAVED_ROUTES = 20;

var REMINDER_GAP_MS = 3 * 60 * 1000;
var PROBLEM_MESSAGES = {
  'OFF ROUTE': 'You are off route',
  'IDLE': 'You have not moved for a while.'
};

var BEEP_PITCHES = [880, 660, 880];
var CODE_LETTERS = 'abcdefghijkmnpqrstuvwxyz23456789';
var MAP_START = { lat: 49.19, lng: -122.85 };
var COMMUTER_ICON = 'https://maps.google.com/mapfiles/ms/icons/red-dot.png';

var role = '';
var code = '';
var currentUser = null;
var audioContext = null;




function showScreen(id) {
  var screens = document.querySelectorAll('.screen');
  for (var i = 0; i < screens.length; i++) {
    screens[i].hidden = screens[i].id !== id;
  }
}

function chooseRole(chosen) {
  role = chosen;
  showScreen('screenCode');
}

function makeCode() {
  var bytes = new Uint8Array(10);
  crypto.getRandomValues(bytes);

  var newCode = '';
  for (var i = 0; i < bytes.length; i++) {
    newCode = newCode + CODE_LETTERS[bytes[i] % CODE_LETTERS.length];
  }
  document.getElementById('codeBox').value = newCode;
}

async function goNext() {
  code = document.getElementById('codeBox').value.trim().toLowerCase();
  var error = document.getElementById('codeError');

  if (!/^[a-z0-9]{6,40}$/.test(code)) {
    error.textContent = 'Code must be 6+ letters or numbers.';
    return;
  }
  error.textContent = '';

  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
  }

  var data;
  try {
    data = await loadData();
  } catch (problem) {
    error.textContent = 'Could not reach the server. Try again.';
    return;
  }
  if (currentUser) return;

  if (role === 'commuter') {
    showScreen('screenCommuter');
    currentUser = new Commuter();
  } else {
    showScreen('screenCaretaker');
    currentUser = new Caretaker();
  }
  currentUser.start(data);
}

function goBack() {
  if (currentUser.routeStarted && !confirm('Going back will stop your route. Go back anyway?')) {
    return;
  }
  currentUser.stop();
  currentUser = null;
  showScreen('screenRole');
}




// server requests and /data folder 
async function loadData() {
  var reply = await fetch('/data?code=' + code);
  if (!reply.ok) throw new Error('Could not load data');
  return await reply.json();
}

async function saveData(key, value) {
  var reply = await fetch('/data?code=' + code, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ key: key, value: value })
  });
  if (!reply.ok) throw new Error('Could not save data');
}



//code from prototype fixed
function toMetres(point, referenceLat) {
  return {
    x: point.lng * 111320 * Math.cos(referenceLat * Math.PI / 180),
    y: point.lat * 110540
  };
}

function metresBetween(a, b) {
  var p = toMetres(a, a.lat);
  var q = toMetres(b, a.lat);
  return Math.sqrt((p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y));
}

function distanceToSegment(p, a, b) {
  var dx = b.x - a.x;
  var dy = b.y - a.y;
  var lengthSquared = dx * dx + dy * dy;

  var t = 0;
  if (lengthSquared > 0) {
    t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSquared;
    if (t < 0) t = 0;
    if (t > 1) t = 1;
  }

  var closestX = a.x + t * dx;
  var closestY = a.y + t * dy;
  return Math.sqrt((p.x - closestX) * (p.x - closestX) + (p.y - closestY) * (p.y - closestY));
}

function distanceFromRoute(position, path) {
  var p = toMetres(position, position.lat);
  var closest = Infinity;

  for (var i = 0; i < path.length - 1; i++) {
    var a = toMetres(path[i], position.lat);
    var b = toMetres(path[i + 1], position.lat);
    var distance = distanceToSegment(p, a, b);
    if (distance < closest) closest = distance;
  }
  return closest;
}


// maps and routes and api 
function makeMap(elementId) {
  return new google.maps.Map(document.getElementById(elementId), {
    center: MAP_START,
    zoom: 15
  });
}

function drawRoute(map, oldLine, path) {
  if (oldLine) oldLine.setMap(null);

  var line = new google.maps.Polyline({
    map: map,
    path: path,
    strokeColor: '#15b548',
    strokeWeight: 6
  });

  var bounds = new google.maps.LatLngBounds();
  for (var i = 0; i < path.length; i++) bounds.extend(path[i]);
  map.fitBounds(bounds);

  return line;
}


//putting info on cards, labelled as .note on css
function timeText(timestamp) {
  return new Date(timestamp).toLocaleTimeString();
}

function noteCards(notes) {
  var cards = [];
  for (var i = notes.length - 1; i >= 0; i--) {
    cards.push([notes[i].text, timeText(notes[i].time)]);
  }
  return cards;
}

function tripCards(routes) {
  var cards = [];
  for (var i = routes.length - 1; i >= 0; i--) {
    cards.push([
      'To: ' + routes[i].destination,
      'Note: ' + (routes[i].note || '(none)'),
      timeText(routes[i].time)
    ]);
  }
  return cards;
}

function showCards(containerId, cards, emptyText) {
  var container = document.getElementById(containerId);
  container.innerHTML = '';

  if (cards.length === 0) {
    container.textContent = emptyText;
    return;
  }

  for (var i = 0; i < cards.length; i++) {
    var card = document.createElement('div');
    card.className = 'note';

    for (var j = 0; j < cards[i].length; j++) {
      var line = document.createElement('p');
      line.textContent = cards[i][j];
      card.appendChild(line);
    }
    container.appendChild(card);
  }
}


// reminders and audio cues needs fixes and to be tested better
function unlockAudio() {
  if (!audioContext) audioContext = new AudioContext();
  audioContext.resume();
}

function playBeeps() {
  if (!audioContext || audioContext.state !== 'running') return;

  for (var i = 0; i < BEEP_PITCHES.length; i++) {
    var start = audioContext.currentTime + i * 0.3;
    var oscillator = audioContext.createOscillator();
    var volume = audioContext.createGain();

    oscillator.frequency.value = BEEP_PITCHES[i];
    volume.gain.setValueAtTime(0.3, start);
    volume.gain.exponentialRampToValueAtTime(0.001, start + 0.2);

    oscillator.connect(volume);
    volume.connect(audioContext.destination);
    oscillator.start(start);
    oscillator.stop(start + 0.2);
  }
}

async function showNotification(title, body, tag) {
  if (navigator.vibrate) navigator.vibrate([300, 150, 300]);
  if (!('Notification' in window) || Notification.permission !== 'granted') return;

  try {
    var registration = null;
    if ('serviceWorker' in navigator) registration = await navigator.serviceWorker.getRegistration();

    if (registration) {
      registration.showNotification(title, { body: body, tag: tag, renotify: true });
    } else {
      new Notification(title, { body: body, tag: tag });
    }
  } catch (error) {
    console.log(error);
  }
}


// messy but i attempted to try and make a commuter class (this comment has to go before we submit)
class Commuter {
  constructor() {
    this.data = {};
    this.map = null;
    this.marker = null;
    this.position = null;
    this.watchId = null;
    this.timerId = null;
    this.wakeLock = null;
    this.route = null;
    this.routeLine = null;
    this.routeStarted = false;
    this.arrived = false;
    this.lastMoved = null;
    this.lastReminder = {};
    this.stopped = false;
  }

  start(data) {
    this.data = data;
    this.map = makeMap('mapCommuter');
    this.marker = new google.maps.Marker({ map: this.map });

    document.getElementById('warning').hidden = true;
    document.getElementById('myStatus').textContent = 'Finding your location...';
    this.showMessage('');
    this.showTrips();
    this.showNotes();

    var routes = this.data.routes || [];
    if (routes.length > 0) {
      this.route = routes[routes.length - 1];
      this.routeLine = drawRoute(this.map, this.routeLine, this.route.path);
    }

    var me = this;
    this.watchId = navigator.geolocation.watchPosition(
      function (result) { me.gotPosition(result); },
      function () { document.getElementById('myStatus').textContent = 'Allow location access to use NoteMap.'; },
      { enableHighAccuracy: true }
    );

    this.loop();
  }

  async stop() {
    this.stopped = true;
    clearTimeout(this.timerId);
    navigator.geolocation.clearWatch(this.watchId);
    if (this.wakeLock) this.wakeLock.release();

    if (this.position) {
      this.routeStarted = false;
      this.arrived = false;
      try {
        await this.sendStatus([]);
      } catch (error) {
        console.log(error);
      }
    }
  }

  showMessage(text) {
    document.getElementById('commuterMessage').textContent = text;
  }

  showTrips() {
    showCards('tripList', tripCards(this.data.routes || []), 'No trips yet.');
  }

  showNotes() {
    showCards('notesForCommuter', noteCards(this.data.caretakerNotes || []), 'No notes yet.');
  }


  gotPosition(result) {
    this.position = { lat: result.coords.latitude, lng: result.coords.longitude };
    this.marker.setPosition(this.position);

    if (this.lastMoved === null) {
      this.map.setCenter(this.position);
      this.lastMoved = { position: this.position, time: Date.now() };


    } else if (metresBetween(this.position, this.lastMoved.position) > IDLE_RADIUS_METRES) {
      this.lastMoved = { position: this.position, time: Date.now() };
    }

    document.getElementById('myStatus').textContent = 'Location found.';
  }

  async makeRoute() {
    if (!this.position) {
      this.showMessage('Still finding your location. Try again in a moment.');
      return;
    }
    var destination = document.getElementById('destination').value.trim();
    if (!destination) {
      this.showMessage('Enter a destination first.');
      return;
    }

    var button = document.getElementById('makeRouteButton');
    button.disabled = true;
    this.showMessage('Finding a route...');

    try {
      var library = await google.maps.importLibrary('routes');

      var answer = await library.Route.computeRoutes({
        origin: this.position,
        destination: destination,
        travelMode: document.getElementById('mode').value,
        fields: ['path']
      });


      var points = answer.routes[0].path;

      var path = [];
      for (var i = 0; i < points.length; i++) {
        path.push({ lat: Number(points[i].lat), lng: Number(points[i].lng) });
      }

      var route = {
        destination: destination,
        note: document.getElementById('routeNote').value.trim(),
        path: path,
        time: Date.now()
      };


      var routes = (this.data.routes || []).concat([route]).slice(-MAX_SAVED_ROUTES);
      await saveData('routes', routes);

      this.data.routes = routes;
      this.route = route;
      this.routeStarted = false;
      this.arrived = false;
      this.routeLine = drawRoute(this.map, this.routeLine, path);
      this.showTrips();
      this.showMessage('Route ready. Press Start route when you set off.');
    } catch (error) {
      console.log(error);
      this.showMessage('Could not make that route. Check the destination, and that the Routes API is enabled for your key.');
    }

    button.disabled = false;

  }

  startRoute() {
    if (!this.route) {
      this.showMessage('Make a route first.');
      return;
    }
    if (!this.position) {
      this.showMessage('Still finding your location. Try again in a moment.');
      return;
    }
    unlockAudio();
    this.keepScreenOn();
    this.routeStarted = true;
    this.arrived = false;
    this.lastMoved = { position: this.position, time: Date.now() };
    this.lastReminder = {};
    this.showMessage('Route started. Keep this page open so reminders can reach you.');
  }
  async keepScreenOn() {
    if (!navigator.wakeLock) return;
    try {
      this.wakeLock = await navigator.wakeLock.request('screen');
    } catch (error) {
      console.log(error);
    }
  }

  async loop() {
    try {
      await this.check();
    } catch (error) {
      console.log(error);
    }

    if (this.stopped) return;
    var me = this;
    this.timerId = setTimeout(function () { me.loop(); }, CHECK_EVERY_MS);
  }

  async check() {
    if (!this.position) return;

    this.checkArrival();
    var problems = this.findProblems();
    this.showWarning(problems);
    this.sendReminders(problems);

    await this.sendStatus(problems);
    await this.getNotes();
  }

  checkArrival() {
    if (!this.routeStarted) return;

    var path = this.route.path;
    if (metresBetween(this.position, path[path.length - 1]) > ARRIVED_METRES) return;

    this.routeStarted = false;
    this.arrived = true;
    if (this.wakeLock) this.wakeLock.release();
    this.showMessage('You have arrived.');
    showNotification('NoteMap', 'You have arrived at your destination.', 'arrived');
  }

  findProblems() {
    var problems = [];
    if (!this.routeStarted) return problems;

    if (distanceFromRoute(this.position, this.route.path) > OFF_ROUTE_METRES) {
      problems.push('OFF ROUTE');
    }
    var secondsStill = (Date.now() - this.lastMoved.time) / 1000;
    if (secondsStill > IDLE_SECONDS) {
      problems.push('IDLE');
    }
    return problems;
  }

  showWarning(problems) {
    var messages = [];
    for (var i = 0; i < problems.length; i++) {
      messages.push(PROBLEM_MESSAGES[problems[i]]);
    }

    var warning = document.getElementById('warning');
    warning.hidden = problems.length === 0;
    warning.textContent = messages.join(' ');
  }

  sendReminders(problems) {
    for (var i = 0; i < problems.length; i++) {
      var problem = problems[i];
      var lastSent = this.lastReminder[problem] || 0;
      if (Date.now() - lastSent < REMINDER_GAP_MS) continue;

      this.lastReminder[problem] = Date.now();
      playBeeps();
      showNotification('NoteMap reminder', PROBLEM_MESSAGES[problem], problem);
    }
  }

  statusText(problems) {
    if (problems.length > 0) return problems.join(' + ');
    if (this.routeStarted) return 'On route';
    if (this.arrived) return 'Arrived';
    return 'Not on a route';
  }

  async sendStatus(problems) {
    await saveData('status', {
      lat: this.position.lat,
      lng: this.position.lng,
      text: this.statusText(problems),
      problems: problems,
      time: Date.now()
    });
  }

  async getNotes() {
    var oldCount = (this.data.caretakerNotes || []).length;
    var latest = await loadData();
    if (this.stopped) return;

    var notes = latest.caretakerNotes || [];
    this.data.caretakerNotes = notes;
    this.showNotes();

    if (notes.length > oldCount) {
      showNotification('Note from your caretaker', notes[notes.length - 1].text, 'caretaker-note');
    }
  }
}


// same as the commuter comment (bit messy but i attempted to try and make it a class (this comment has to go before we submit))
class Caretaker {
  constructor() {
    this.data = {};
    this.map = null;
    this.marker = null;
    this.routeLine = null;
    this.drawnRouteTime = null;
    this.oldProblems = [];
    this.timerId = null;
    this.stopped = false;
  }

  start(data) {
    this.data = data;
    this.map = makeMap('mapCaretaker');
    this.marker = new google.maps.Marker({ map: this.map, icon: COMMUTER_ICON });

    document.getElementById('commuterStatus').textContent = 'Waiting for the commuter to connect...';
    document.getElementById('noteError').textContent = '';
    this.show();
    this.loop();
  }

  stop() {
    this.stopped = true;
    clearTimeout(this.timerId);
  }

  async loop() {
    try {
      var latest = await loadData();
      if (!this.stopped) {
        this.data = latest;
        this.show();
      }
    } catch (error) {
      console.log(error);
    }

    if (this.stopped) return;
    var me = this;
    this.timerId = setTimeout(function () { me.loop(); }, REFRESH_EVERY_MS);
  }

  show() {
    var status = this.data.status;
    if (status) {
      this.marker.setPosition({ lat: status.lat, lng: status.lng });
      document.getElementById('commuterStatus').textContent =
        'Status: ' + status.text + ' (updated ' + timeText(status.time) + ')';
      this.checkForNewProblems(status);
    }

    var routes = this.data.routes || [];
    showCards('routeList', tripCards(routes), 'No routes yet.');

    var newest = routes[routes.length - 1];
    if (newest && newest.time !== this.drawnRouteTime) {
      this.routeLine = drawRoute(this.map, this.routeLine, newest.path);
      this.drawnRouteTime = newest.time;
    }

    showCards('caretakerNoteList', noteCards(this.data.caretakerNotes || []), 'No notes sent yet.');
  }

  checkForNewProblems(status) {
    var problems = status.problems || [];

    for (var i = 0; i < problems.length; i++) {
      if (this.oldProblems.indexOf(problems[i]) === -1) {
        showNotification('Commuter alert', 'Status: ' + status.text, 'commuter-alert');
        break;
      }
    }
    this.oldProblems = problems;
  }

  async addNote() {
    var box = document.getElementById('newNote');
    var text = box.value.trim();
    if (text === '') return;

    var notes = (this.data.caretakerNotes || []).concat([{ text: text, time: Date.now() }]);

    try {
      await saveData('caretakerNotes', notes);
      this.data.caretakerNotes = notes;
      box.value = '';
      document.getElementById('noteError').textContent = '';
      this.show();
    } catch (error) {
      console.log(error);
      document.getElementById('noteError').textContent = 'Could not send the note. Try again.';
    }
  }
}


document.addEventListener('visibilitychange', function () {
  if (document.visibilityState !== 'visible') return;
  if (audioContext) audioContext.resume();
  if (currentUser && currentUser.routeStarted) currentUser.keepScreenOn();
});

// the helper is a service worker for the notifications to work on andriod but this needs to be checked more (comment to be removed before submission)
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/helper.js').catch(function (error) { console.log(error); });
}
