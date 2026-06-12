import Layout from "./Layout";
import { ArrowRight } from "lucide-react";

export default function LongTracksPage() {
  const tracks = [
    "Top 10 March-April 2026",
    "Request March-April 2026",
    "Long Play Japanese Tracks",
    "Community Picks 2026",
  ];
  const art = [
    "/references/InShot_20260525_170921445.jpg",
    "/references/5cad3789ebd0584c96e83a5be506888b.jpg",
    "/assets/fcnami-room-preview.png",
    "/references/InShot_20260525_165025551.jpg",
  ];

  return (
    <Layout>
      <section className="relative pb-20 pt-8">
        <p className="mb-4 max-w-3xl text-[clamp(1.2rem,2vw,2rem)] font-medium leading-snug text-[#303035]">
          หน้านี้รวมเพลย์ลิสต์ยาวและคลิปรวมเพลง สำหรับคนที่อยากเปิดฟังต่อเนื่อง
        </p>
        <div className="flex justify-center mb-16">
          <h1 className="outline-title text-[clamp(4rem,7vw,8.4rem)] font-black uppercase leading-none">
            LONG TRACKS
          </h1>
        </div>

        <div className="grid grid-cols-1 gap-x-12 gap-y-16 lg:grid-cols-2">
          {tracks.map((track, index) => (
            <div key={track} className="flex flex-col gap-6">
              <div className="aspect-video overflow-hidden bg-[#e8e8e5]">
                <img src={art[index]} alt="" className="h-full w-full object-cover grayscale" />
              </div>
              <a href="https://www.youtube.com/@FCNami_TT" target="_blank" rel="noreferrer" className="pill-button w-full text-xl sm:text-2xl">
                {track}
                <ArrowRight size={28} />
              </a>
            </div>
          ))}
        </div>
      </section>
    </Layout>
  );
}
