import Layout from "./Layout";
import { ArrowRight, Eye, ThumbsUp } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { demoSongs } from "../api";

export default function PlayerPage() {
  const { slug } = useParams();
  const song = demoSongs.find((item) => item.slug === slug) ?? demoSongs[0];
  const recommendations = demoSongs.filter((item) => item.slug !== song.slug);
  const art = [
    "/references/InShot_20260525_165025551.jpg",
    "/references/InShot_20260525_170921445.jpg",
    "/references/5cad3789ebd0584c96e83a5be506888b.jpg",
  ];

  return (
    <Layout>
      <section className="grid grid-cols-1 gap-12 pb-20 pt-8 lg:grid-cols-[1fr_420px]">
        <div className="flex flex-col gap-8">
          <div className="relative flex aspect-video items-center justify-center rounded-[40px] border-[14px] border-[#202020] bg-white p-5 shadow-2xl">
            <div className="h-full w-full overflow-hidden bg-[#f3f3f0]">
              {song.youtubeVideoId ? (
                <iframe
                  className="h-full w-full"
                  src={`https://www.youtube.com/embed/${song.youtubeVideoId}`}
                  title={`${song.title} video`}
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                />
              ) : (
                <img src="/assets/fcnami-room-preview.png" alt="" className="h-full w-full object-cover" />
              )}
            </div>
          </div>

          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
              <div className="flex flex-col gap-2">
                <h1 className="text-[clamp(1.8rem,3vw,3.2rem)] font-black">{song.title} - {song.artist} ซับไทย</h1>
                <div className="flex flex-wrap items-center gap-3">
                  {song.tags.map((tag) => (
                    <span key={tag} className="rounded-full bg-[#d8d8d3] px-4 py-2 text-xs font-black uppercase">{tag}</span>
                  ))}
                  <a href={song.youtubeUrl ?? "https://www.youtube.com/@FCNami_TT"} target="_blank" rel="noreferrer" className="pill-button min-h-10 px-5 text-sm">
                    Open YouTube <ArrowRight size={16} />
                  </a>
                </div>
              </div>
              <div className="flex items-center gap-8 text-[#202020]">
                <div className="flex flex-col items-center gap-1">
                  <ThumbsUp size={28} />
                  <span className="text-xs font-bold">LIKE</span>
                </div>
                <div className="flex flex-col items-center gap-1">
                  <Eye size={28} />
                  <span className="text-xs font-bold">VIEWS</span>
                </div>
              </div>
            </div>

            <div className="grid gap-4 rounded-[28px] border-2 border-[#202020] bg-white p-6 text-lg font-medium text-[#4b4b51]">
              <h2 className="text-2xl font-black text-[#202020]">Lyrics Preview</h2>
              <p>{song.lyrics?.kanji || "Kanji lyrics will be added by admin."}</p>
              <p>{song.lyrics?.romaji || "Romaji lyrics will be added by admin."}</p>
              <p>{song.lyrics?.thai || "Thai translation will be added by admin."}</p>
            </div>
          </div>
        </div>

        <aside className="flex flex-col gap-6">
          <h3 className="text-xl font-black uppercase">Recommended</h3>
          <div className="flex flex-col gap-4">
            {recommendations.map((item, index) => (
              <Link key={item.slug} to={`/songs/${item.slug}`} className="group flex gap-4">
                <div className="h-24 w-36 flex-shrink-0 overflow-hidden bg-[#e8e8e5]">
                  <img src={art[index % art.length]} alt="" className="h-full w-full object-cover grayscale transition-all group-hover:grayscale-0" />
                </div>
                <h4 className="text-base font-bold leading-snug transition-colors group-hover:text-[#08c765]">
                  {item.title} - {item.artist} ซับไทย
                </h4>
              </Link>
            ))}
          </div>
        </aside>
      </section>
    </Layout>
  );
}
