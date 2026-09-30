"""Local-only SMTP inbox for synthetic smoke tests. Never forwards mail.

Run before smoke_apis.py. Captured messages include test passwords and stay under target/.
"""
import argparse
import json
from pathlib import Path
import socketserver
import threading

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--port', type=int, default=1025)
parser.add_argument('--output', default='target/smtp-test-messages.jsonl')
args = parser.parse_args()
output = Path(args.output)
output.parent.mkdir(parents=True, exist_ok=True)
lock = threading.Lock()

class SMTP(socketserver.StreamRequestHandler):
    def send(self, value):
        self.wfile.write((value + '\r\n').encode())
        self.wfile.flush()

    def handle(self):
        self.request.settimeout(15)
        self.send('220 localhost EHS synthetic test inbox')
        recipients = []
        while True:
            line = self.rfile.readline(65536)
            if not line:
                return
            command = line.decode('utf-8', errors='replace').strip()
            verb = command.split(' ', 1)[0].upper()
            if verb in ('EHLO', 'HELO'):
                self.send('250 localhost')
            elif verb == 'MAIL':
                recipients = []
                self.send('250 OK')
            elif verb == 'RCPT':
                recipients.append(command.split(':', 1)[1].strip().strip('<>'))
                self.send('250 OK')
            elif verb == 'DATA':
                self.send('354 End with a dot')
                data = bytearray()
                while True:
                    part = self.rfile.readline(65536)
                    if not part:
                        return
                    if part == b'.\r\n':
                        break
                    data.extend(part[1:] if part.startswith(b'..') else part)
                    if len(data) > 1024 * 1024:
                        return
                with lock, output.open('a', encoding='utf-8') as stream:
                    stream.write(json.dumps({'to': recipients, 'message': data.decode('utf-8', errors='replace')}) + '\n')
                self.send('250 Captured locally')
            elif verb == 'QUIT':
                self.send('221 Bye')
                return
            elif verb in ('RSET', 'NOOP'):
                self.send('250 OK')
            else:
                self.send('502 Unsupported')

class Server(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True

with Server(('127.0.0.1', args.port), SMTP) as server:
    print(f'Local SMTP test inbox listening on 127.0.0.1:{args.port}; no external delivery.', flush=True)
    server.serve_forever()
