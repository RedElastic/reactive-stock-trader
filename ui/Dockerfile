FROM node:8
RUN mkdir -p /root/src
RUN apt-get update && apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_8.x | bash -
RUN apt-get update && apt-get install -y \
  nodejs \
  npm
COPY . /root/src/
WORKDIR /root/src/ui
RUN npm install
EXPOSE 8080
CMD ["npm", "run", "serve"]