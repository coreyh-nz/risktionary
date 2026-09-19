# Stage 1: Build
FROM node:25-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .

ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_WS_URL
ARG NEXT_PUBLIC_PERSIST_GAME_SESSION

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_WS_URL=$NEXT_PUBLIC_WS_URL
ENV NEXT_PUBLIC_PERSIST_GAME_SESSION=$NEXT_PUBLIC_PERSIST_GAME_SESSION

RUN npm run build

# Stage 2: Run
FROM node:25-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 3000

CMD ["node", "server.js"]