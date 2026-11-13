FROM node:22-slim
ENV NG_CLI_ANALYTICS=false
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Compila una vez para que un error de TypeScript haga fallar la construcción de la imagen.
RUN npx ng build --configuration development
EXPOSE 4200
CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--port", "4200"]
