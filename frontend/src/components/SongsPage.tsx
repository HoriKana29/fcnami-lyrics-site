import Layout from './Layout';
import { ArrowDownRight, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { demoSongs } from '../api';

const buttons = [
  { label: 'Popular Songs', thai: 'เพลงยอดนิยม', to: '/search?sort=popular' },
  { label: 'Long Track', thai: 'เพลงยาว', to: '/long-tracks' },
  { label: 'Requested Songs', thai: 'เพลงที่รีเควส', to: '/queue' },
  { label: 'Bandori Music', thai: 'เพลง Bandori', to: '/search?tag=bandori' },
];

export default function SongsPage() {
  const art = [
    '/references/InShot_20260525_170921445.jpg',
    '/references/5cad3789ebd0584c96e83a5be506888b.jpg',
    '/references/c79b47b9079bfce8f0c0a72ee4ba2e9a (1).jpg',
    '/references/InShot_20260525_165025551.jpg',
    '/assets/fcnami-room-preview.png',
    '/references/InShot_20260525_170921445.jpg',
  ];

  return (
    <Layout>
      <section className='grid grid-cols-1 gap-12 pb-20 pt-8 lg:grid-cols-[0.9fr_1fr]'>
        <div className='flex flex-col gap-12'>
          <div className='relative'>
            <div className='mb-12 h-2 w-24 bg-black' />
            <h1 className='big-title'>SONGS</h1>
          </div>
          <div className='flex flex-col gap-4'>
            {buttons.map((btn) => (
              <div key={btn.label} className='group flex flex-wrap items-center gap-6'>
                <Link to={btn.to} className='pill-button w-full max-w-[520px] text-xl sm:text-2xl'>
                  {btn.label}
                  <ArrowDownRight size={34} />
                </Link>
                <span className='text-lg font-medium text-[#77777d]'>{btn.thai}</span>
              </div>
            ))}
          </div>

          <div className='grid gap-3'>
            {demoSongs.map((song) => (
              <Link key={song.slug} to={`/songs/${song.slug}`} className='flex items-center justify-between border-b border-[#202020]/20 py-4 text-lg font-bold'>
                <span>{song.title} - {song.artist}</span>
                <ArrowRight size={22} />
              </Link>
            ))}
          </div>
        </div>

        <div className='grid min-h-[560px] grid-cols-2 grid-rows-4 overflow-hidden bg-[#e8e8e5] lg:grid-cols-3'>
          {art.map((asset, index) => (
            <div key={`${asset}-${index}`} className={`${index === 0 ? 'col-span-2' : ''} overflow-hidden`}>
              <img src={asset} alt='' className='h-full w-full object-cover' />
            </div>
          ))}
        </div>
      </section>
    </Layout>
  );
}
