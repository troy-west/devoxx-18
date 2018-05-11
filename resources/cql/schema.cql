CREATE TABLE message (
    sender text,
    date text,
    word text,
    message_id text,
    recipient text,
    subject text,
    PRIMARY KEY ((sender, date), word, message_id));