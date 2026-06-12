import Layout from "./Layout";
import { ArrowRight, ListMusic, Search, Youtube } from "lucide-react";
import { Link } from "react-router-dom";

export default function HomePage() {
  const tiles = [
    "/references/InShot_20260525_165025551.jpg",
    "/references/InShot_20260525_170921445.jpg",
    "/references/5cad3789ebd0584c96e83a5be506888b.jpg",
    "/references/c79b47b9079bfce8f0c0a72ee4ba2e9a (1).jpg",
    "/assets/fcnami-room-preview.png",
    "/references/InShot_20260525_165025551.jpg",
    "/references/InShot_20260525_170921445.jpg",
    "/references/5cad3789ebd0584c96e83a5be506888b.jpg",
    "/references/c79b47b9079bfce8f0c0a72ee4ba2e9a (1).jpg",
  ];

  return (
    <Layout>
      <section className="grid min-h-[calc(100vh-132px)] grid-cols-1 gap-12 pb-16 lg:grid-cols-[0.72fr_1fr] lg:items-stretch">
        <div className="flex flex-col justify-between gap-12">
          <div className="max-w-[620px] pt-10">
            <div className="mb-12 h-2 w-28 bg-black" />
            <p className="mb-10 text-[clamp(1.3rem,2vw,2.05rem)] leading-snug text-[#2f2f33]">
              เพลงญี่ปุ่น ซับไทย คลังเนื้อเพลง และคิวรีเควสของชุมชน FCNami T_T
            </p>
          </div>

          <div className="space-y-8">
            <a
              href="https://www.youtube.com/@FCNami_TT"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-5 text-[clamp(2.4rem,4vw,5rem)] font-black"
            >
              <Youtube className="h-[0.9em] w-[0.9em]" fill="currentColor" />
              YouTube
            </a>
            <h1 className="big-title text-[#08c765]">
              FCNAMI
              <br />
              T_T
            </h1>
          </div>

          <div className="flex flex-wrap gap-4 pb-6">
            <Link to="/songs" className="pill-button">
              <ListMusic size={24} />
              Browse Songs
              <ArrowRight size={24} />
            </Link>
            <Link to="/queue" className="pill-button bg-[#08c765] text-black hover:bg-[#06b65c]">
              <Search size={24} />
              Check Queue
              <ArrowRight size={24} />
            </Link>
          </div>
        </div>

        <div className="grid min-h-[560px] grid-cols-3 grid-rows-4 gap-0 overflow-hidden">
          {tiles.map((tile, index) => (
            <div
              key={`${tile}-${index}`}
              className={`${index === 1 || index === 4 ? "col-span-2" : ""} ${index === 5 ? "row-span-2" : ""} overflow-hidden bg-[#e8e8e5]`}
            >
              <img
                src={tile}
                alt=""
                className="h-full w-full object-cover transition duration-300 hover:scale-[1.03]"
              />
            </div>
          ))}
        </div>
      </section>
    </Layout>
  );
}
