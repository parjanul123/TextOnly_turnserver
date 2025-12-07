# Folosește imaginea oficială Coturn
FROM instrumentisto/coturn

# Expune porturile pentru TURN/STUN
EXPOSE 3478
EXPOSE 5349

# Pornește Coturn cu setări simple
CMD ["turnserver",
"--no-cli",
"--no-tls",
"--no-dtls",
"--listening-port", "3478",
"--realm", "textonly.onrender.com",
"--user", "demo:demo1234",
"--lt-cred-mech",
"--fingerprint",
"--verbose"]
