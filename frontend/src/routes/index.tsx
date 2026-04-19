import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { useAuth } from "@/lib/use-auth";
import { Brand } from "@/components/brand";
import { Button } from "@/components/ui/inputs/button";
import {
  ArrowRight,
  Lock,
  Sparkles,
  LineChart,
  BookOpen,
  Heart,
  Shield,
} from "lucide-react";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "MindTrack — A private journal for your inner weather" },
      {
        name: "description",
        content:
          "Write privately. See your emotional weather. Owned by you. Encrypted journaling with self-hosted sentiment analytics.",
      },
    ],
  }),
  component: Landing,
});

function Landing() {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  // If already logged in, go straight to the app
  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      navigate({ to: "/app" });
    }
  }, [isLoading, isAuthenticated, navigate]);

  return (
    <div className="min-h-screen bg-background">
      <Header />
      <Hero />
      <Features />
      <PrivacyBand />
      <FinalCta />
      <Footer />
    </div>
  );
}

function Header() {
  return (
    <header className="sticky top-0 z-30 border-b border-border/40 bg-background/70 backdrop-blur-xl">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <Brand />
        <nav className="hidden items-center gap-8 text-sm text-muted-foreground md:flex">
          <a href="#features" className="hover:text-foreground">
            Features
          </a>
          <a href="#privacy" className="hover:text-foreground">
            Privacy
          </a>
          {/* <Link to="/admin/login" className="hover:text-foreground">
            Admin
          </Link> */}
        </nav>
        <div className="flex items-center gap-2">
          <Button asChild variant="ghost" size="sm">
            <Link to="/login">Sign in</Link>
          </Button>
          <Button asChild size="sm" className="rounded-full shadow-bloom">
            <Link to="/register">
              Begin <ArrowRight className="ml-1 h-3.5 w-3.5" />
            </Link>
          </Button>
        </div>
      </div>
    </header>
  );
}

function Hero() {
  return (
    <section className="relative overflow-hidden">
      <div className="pointer-events-none absolute inset-0 bg-gradient-dusk opacity-60" />
      <div className="pointer-events-none absolute -top-40 left-1/2 h-[500px] w-[800px] -translate-x-1/2 rounded-full bg-rose/30 blur-[140px]" />
      <div className="relative mx-auto max-w-5xl px-6 py-24 text-center md:py-36">
        <span className="inline-flex animate-bloom-in items-center gap-2 rounded-full border border-border bg-card/60 px-4 py-1.5 text-xs uppercase tracking-[0.18em] text-muted-foreground backdrop-blur">
          <Sparkles className="h-3 w-3" /> Private by design
        </span>
        <h1 className="mt-8 animate-bloom-in font-serif text-5xl font-medium leading-[1.05] tracking-tight text-balance text-foreground md:text-7xl lg:text-8xl">
          A quiet place
          <br />
          for <em className="text-bloom not-italic">your</em> inner weather.
        </h1>
        <p className="mx-auto mt-7 max-w-xl animate-bloom-in text-lg leading-relaxed text-muted-foreground md:text-xl">
          MindTrack is an encrypted journal that listens — gently surfacing the
          patterns in your emotions without ever showing them to anyone else.
        </p>
        <div className="mt-10 flex flex-wrap items-center justify-center gap-3">
          <Button asChild size="lg" className="rounded-full px-7 shadow-bloom">
            <Link to="/register">
              Start your journal <ArrowRight className="ml-1.5 h-4 w-4" />
            </Link>
          </Button>
          <Button
            asChild
            size="lg"
            variant="ghost"
            className="rounded-full px-7"
          >
            <Link to="/login">I already write here</Link>
          </Button>
        </div>
        <div className="mt-14 flex items-center justify-center gap-6 text-xs uppercase tracking-widest text-muted-foreground/70">
          <span className="flex items-center gap-1.5">
            <Lock className="h-3 w-3" /> End-to-end
          </span>
          <span className="h-1 w-1 rounded-full bg-muted-foreground/40" />
          <span className="flex items-center gap-1.5">
            <Shield className="h-3 w-3" /> Self-hosted AI
          </span>
          <span className="h-1 w-1 rounded-full bg-muted-foreground/40" />
          <span className="flex items-center gap-1.5">
            <Heart className="h-3 w-3" /> Forever yours
          </span>
        </div>
      </div>
    </section>
  );
}

const features = [
  {
    icon: BookOpen,
    title: "Distraction-free writing",
    body: "A clean canvas with rich text, headings, quotes and images. One entry per moment, kept just as you wrote it.",
  },
  {
    icon: Sparkles,
    title: "Quiet emotion analysis",
    body: "Every entry is read by a private model that maps sentiment and dominant emotions — never sent to a third party.",
  },
  {
    icon: LineChart,
    title: "Your emotional weather",
    body: "A mood calendar, sentiment trends, and emotion distribution let you see the shape of your weeks and months.",
  },
  {
    icon: Lock,
    title: "Encrypted and yours",
    body: "Application-level encryption before storage, strict ownership, and a one-click right-to-be-forgotten.",
  },
];

function Features() {
  return (
    <section id="features" className="relative mx-auto max-w-6xl px-6 py-24">
      <div className="mb-14 max-w-2xl">
        <p className="mb-3 text-xs uppercase tracking-[0.2em] text-bloom">
          What it gives you
        </p>
        <h2 className="font-serif text-4xl font-medium leading-tight text-balance md:text-5xl">
          A journal that remembers, gently.
        </h2>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        {features.map((f) => (
          <div
            key={f.title}
            className="group relative overflow-hidden rounded-3xl border border-border bg-card p-7 shadow-soft transition-all hover:-translate-y-0.5 hover:shadow-elevated"
          >
            <div className="mb-5 grid h-11 w-11 place-items-center rounded-2xl bg-secondary text-primary transition-colors group-hover:bg-gradient-bloom group-hover:text-primary-foreground">
              <f.icon className="h-5 w-5" strokeWidth={1.8} />
            </div>
            <h3 className="font-serif text-2xl font-medium">{f.title}</h3>
            <p className="mt-2 leading-relaxed text-muted-foreground">
              {f.body}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
}

function PrivacyBand() {
  return (
    <section id="privacy" className="relative mx-auto max-w-6xl px-6 py-20">
      <div className="relative overflow-hidden rounded-[2rem] bg-gradient-bloom p-10 text-primary-foreground shadow-elevated md:p-16">
        <div className="pointer-events-none absolute -right-20 -top-20 h-80 w-80 rounded-full bg-white/10 blur-3xl" />
        <div className="relative max-w-2xl">
          <p className="mb-3 text-xs uppercase tracking-[0.2em] text-primary-foreground/70">
            Privacy first, always
          </p>
          <h2 className="font-serif text-4xl font-medium leading-tight md:text-5xl">
            Your text never leaves your infrastructure.
          </h2>
          <p className="mt-5 text-lg leading-relaxed text-primary-foreground/80">
            Sentiment analysis runs on a self-hosted model. JWTs are signed and
            short-lived, content is encrypted before it touches the database,
            and only you can read it.
          </p>
        </div>
      </div>
    </section>
  );
}

function FinalCta() {
  return (
    <section className="mx-auto max-w-3xl px-6 py-24 text-center">
      <h2 className="font-serif text-4xl font-medium leading-tight md:text-5xl">
        Begin your quiet practice.
      </h2>
      <p className="mx-auto mt-4 max-w-lg text-lg text-muted-foreground">
        A few minutes a day. A clearer view of yourself over months.
      </p>
      <div className="mt-8 flex justify-center">
        <Button asChild size="lg" className="rounded-full px-8 shadow-bloom">
          <Link to="/register">
            Create your journal <ArrowRight className="ml-1.5 h-4 w-4" />
          </Link>
        </Button>
      </div>
    </section>
  );
}

function Footer() {
  return (
    <footer className="border-t border-border/40 py-10">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-6 text-sm text-muted-foreground md:flex-row">
        <Brand size="sm" />
        <p>© {new Date().getFullYear()} MindTrack. Encrypted with care.</p>
      </div>
    </footer>
  );
}
