/** @type {import('next').NextConfig} */
const nextConfig = {
  devIndicators: false,
  async rewrites() {
    return [
      { source: '/api/task1/:path*', destination: 'http://localhost:8081/api/task1/:path*' },
      { source: '/api/task2/:path*', destination: 'http://localhost:8082/api/task2/:path*' },
      { source: '/api/task3/:path*', destination: 'http://localhost:8083/api/task3/:path*' },
      { source: '/api/task4/:path*', destination: 'http://localhost:8084/api/task4/:path*' },
      { source: '/api/task5/:path*', destination: 'http://localhost:8085/api/task5/:path*' },
    ];
  },
};

module.exports = nextConfig;
