import Layout from './Layout';
import { ArrowRight, Music2, Youtube } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function AboutPage() {
  const art = [
    '/references/InShot_20260525_165025551.jpg',
    '/references/InShot_20260525_170921445.jpg',
    '/references/5cad3789ebd0584c96e83a5be506888b.jpg',
    '/references/c79b47b9079bfce8f0c0a72ee4ba2e9a (1).jpg',
    '/assets/fcnami-room-preview.png',
    '/references/InShot_20260525_170921445.jpg',
  ];

  return (
    <Layout>
      <section className='grid grid-cols-1 gap-12 pb-20 pt-8 lg:grid-cols-[0.8fr_1fr] lg:items-center'>
        <div>
          <div className='mb-12 h-2 w-24 bg-black' />
          <h1 className='big-title'>
            ABOUT
            <br />
            FCNAMI
          </h1>
          <p className='mt-10 max-w-2xl text-xl leading-relaxed text-[#69696f]'>
            FCNami T_T คือพื้นที่รวมเพลงญี่ปุ่นซับไทย เนื้อเพลง Kanji, Romaji, คำแปลไทย และคิวรีเควสจากผู้ชมช่อง YouTube
          </p>
          <div className='mt-10 flex flex-wrap gap-4'>
            <Link to='/songs' className='pill-button'>
              <Music2 size={24} />
              Browse Songs
              <ArrowRight size={24} />
            </Link>
            <a href='https://www.youtube.com/@FCNami_TT' target='_blank' rel='noreferrer' className='pill-button bg-[#08c765] text-black hover:bg-[#06b65c]'>
              <Youtube size={24} />
              YouTube
              <ArrowRight size={24} />
            </a>
          </div>
        </div>
        <div className='grid min-h-[620px] grid-cols-2 grid-rows-3 overflow-hidden bg-[#e8e8e5]'>
          {art.map((asset, index) => (
            <div key={`${asset}-${index}`} className={index === 0 ? 'row-span-2 overflow-hidden' : 'overflow-hidden'}>
              <img src={asset} alt='' className='h-full w-full object-cover' />
            </div>
          ))}
        </div>
      </section>
    </Layout>
  );
}
