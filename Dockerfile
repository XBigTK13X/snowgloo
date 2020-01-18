FROM node:13.6.0

RUN apt-get update && apt-get install nginx ffmpeg -y

COPY script/nginx/site.conf /etc/nginx/sites-enabled/default

WORKDIR /usr/src/app/web-server
COPY ./web-server/package.json ./package.json
COPY ./web-server/yarn.lock ./yarn.lock
RUN yarn install

WORKDIR /usr/src/app/web-client
COPY ./web-client/package.json ./package.json
COPY ./web-client/yarn.lock ./yarn.lock
RUN yarn install

WORKDIR /usr/src/app
COPY ./web-server/src ./web-server/src
COPY ./web-client/src ./web-client/src
COPY ./web-client/public ./web-client/public

WORKDIR /usr/src/app/web-client
RUN yarn build
RUN rm -rf /usr/src/app/web-server/src/web-build/
RUN cp -r /usr/src/app/web-client/build/ /usr/src/app/web-server/src/web-build/

EXPOSE 5050
WORKDIR /usr/src/app/web-server
CMD [ "/bin/bash","-c","nginx && node src/index" ]
