import { baseUrl } from './utils';

export default class websocket {
  constructor(option, callback = null) {
    this.ws = new WebSocket(
      `ws://${baseUrl.substr(6)}fnotify/websocket/${option}`
    );
    this.callback = callback;

    this.ws.onmessage = e => {
      this.onmessage(e, callback);
    };

    this.ws.onerror = err => {
      this.onerror(err);
    };

    this.ws.onclose = msg => {};
  }

  onmessage(evt, fn) {
    fn(evt);
  }

  onerror(err) {}

  onclose() {
    this.ws.close();
  }
}
