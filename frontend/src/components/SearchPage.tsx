import Layout from './Layout';
import { useMemo, useState } from 'react';
import { Search, X, SlidersHorizontal, ArrowUpDown, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { demoSongs } from '../api';

export default function SearchPage() {
  const art = [
    '/references/InShot_20260525_165025551.jpg',
    '/references/InShot_20260525_170921445.jpg',
    '/references/5cad3789ebd0584c96e83a5be506888b.jpg',
  ];
  const [query, setQuery] = useState('');
  const [onlyRequested, setOnlyRequested] = useState(false);
  const [sortPopular, setSortPopular] = useState(false);

  const results = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return demoSongs
      .filter((song) => !needle || [song.title, song.titleJapanese, song.artist, song.sourceAnimeOrGame, ...song.tags].filter(Boolean).some((value) => value!.toLowerCase().includes(needle)))
      .filter((song) => !onlyRequested || song.status !== 'PUBLISHED')
      .sort((a, b) => sortPopular ? b.title.length - a.title.length : a.title.localeCompare(b.title));
  }, [onlyRequested, query, sortPopular]);

  return (
    <Layout>
      <section className='grid min-h-[80vh] grid-cols-1 gap-12 pb-20 pt-8 lg:grid-cols-[minmax(360px,0.9fr)_1fr]'>
        <div className='panel-line flex flex-col gap-8 rounded-[32px] p-8'>
          <div className='flex items-center justify-between'>
            <Search size={34} />
            <div className='flex items-center gap-4 text-[#202020]'>
              <button type='button' onClick={() => setOnlyRequested((value) => !value)} aria-label='Toggle requested songs filter' className={onlyRequested ? 'text-[#08c765]' : ''}>
                <SlidersHorizontal size={28} />
              </button>
              <button type='button' onClick={() => setSortPopular((value) => !value)} aria-label='Toggle popular sorting' className={sortPopular ? 'text-[#08c765]' : ''}>
                <ArrowUpDown size={28} />
              </button>
              <button type='button' onClick={() => setQuery('')} aria-label='Clear search'>
                <X size={32} />
              </button>
            </div>
          </div>

          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            className='min-h-14 rounded-full border-2 border-[#202020] bg-transparent px-5 text-lg outline-none'
            placeholder='ค้นหาเพลง ศิลปิน แท็ก หรืออนิเมะ'
          />
          
          <div className='flex flex-wrap gap-2'>
            <span className={`rounded-full px-4 py-2 text-sm font-black ${sortPopular ? 'bg-[#08c765] text-black' : 'bg-[#ececec]'}`}>Popular</span>
            <span className={`rounded-full px-4 py-2 text-sm font-black ${onlyRequested ? 'bg-[#08c765] text-black' : 'bg-[#ececec]'}`}>Requested</span>
          </div>

          <div className='grid gap-4'>
            {results.map((song, index) => (
              <Link key={song.slug} to={`/songs/${song.slug}`} className='flex items-center gap-5 rounded-2xl border border-[#202020]/15 p-4 hover:bg-[#f0f0ec]'>
                <img src={art[index % art.length]} alt='' className='h-20 w-20 rounded-xl object-cover grayscale' />
                <div className='min-w-0 flex-1'>
                  <h3 className='truncate text-lg font-bold'>{song.title} - {song.artist} ซับไทย</h3>
                  <div className='mt-2 flex flex-wrap gap-2'>
                    {song.tags.slice(0, 2).map((tag) => (
                      <span key={tag} className='rounded-full bg-[#e0e0dc] px-3 py-1 text-xs font-black uppercase'>{tag}</span>
                    ))}
                  </div>
                </div>
                <ArrowRight size={22} />
              </Link>
            ))}
          </div>
        </div>

        <div className='relative min-h-[620px] overflow-hidden'>
          <img src='/references/5cad3789ebd0584c96e83a5be506888b.jpg' alt='' className='h-full w-full object-cover opacity-70' />
          <div className='absolute inset-x-0 top-4 px-6'>
            <h1 className='outline-title text-[clamp(4rem,8vw,9rem)] font-black uppercase leading-none'>
              SEARCHING...
            </h1>
          </div>
        </div>
      </section>
    </Layout>
  );
}
