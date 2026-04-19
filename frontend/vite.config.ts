import { defineConfig, type ConfigEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import tsconfigPaths from "vite-tsconfig-paths";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";

export default async ({ command }: ConfigEnv) => {
  const isBuild = command === "build";
  const plugins = [tanstackStart(), react(), tailwindcss(), tsconfigPaths()];

  if (isBuild) {
    const { cloudflare } = await import("@cloudflare/vite-plugin");
    plugins.push(cloudflare());
  }

  return defineConfig({
    plugins,
    server: {
      host: "::",
      port: 3000,
    },
  });
};
