# frontend/Dockerfile
FROM nginx:1.27-alpine

WORKDIR /usr/share/nginx/html
RUN rm -rf ./*

COPY dist/ ./

COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
