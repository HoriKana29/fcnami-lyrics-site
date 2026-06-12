import { useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import Layout from './Layout';
import { demoQueue } from '../api';
import type { QueueItemResponse } from '../types';
import { ArrowRight, Check, Clock, Search, SlidersHorizontal, Youtube } from 'lucide-react';

export default function QueuePage() {
  const [query, setQuery] = useState('');
  const [tier, setTier] = useState('All');
  const [status, setStatus] = useState('All');

  const tiers = useMemo(() => ['All', ...Array.from(new Set(demoQueue.items.map((item) => displayTier(item.tier))))], []);
  const visibleItems = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return demoQueue.items.filter((item) => {
      const itemTier = displayTier(item.tier);
      const itemStatus = item.translated ? 'Done' : 'Waiting';
      const matchesTier = tier === 'All' || itemTier === tier;
      const matchesStatus = status === 'All' || itemStatus === status;
      const matchesQuery =
        !needle ||
        [item.songTitle, item.requester, item.status, itemTier]
          .filter(Boolean)
          .some((value) => value!.toLowerCase().includes(needle));
      return matchesTier && matchesStatus && matchesQuery;
    });
  }, [query, tier, status]);

  const doneCount = demoQueue.items.filter((item) => item.translated).length;
  const waitingCount = demoQueue.items.length - doneCount;

  return (
    <Layout>
      <section className='pb-20 pt-8'>
        <div className='mb-12 flex flex-col gap-8 lg:flex-row lg:items-end lg:justify-between'>
          <div>
            <h1 className='outline-title text-[clamp(4rem,7vw,8.4rem)] font-black uppercase leading-none'>REQUESTS LIST</h1>
            <p className='mt-5 max-w-3xl text-xl leading-relaxed text-[#69696f]'>
              คิวรีเควสจาก Google Sheet แสดงในหน้าเว็บเพื่อค้นหาเพลง ผู้รีเควส และสถานะได้ง่ายขึ้น
            </p>
          </div>
          <div className='grid grid-cols-3 gap-3 text-center'>
            <Summary label='ทั้งหมด' value={demoQueue.total} />
            <Summary label='แปลแล้ว' value={doneCount} />
            <Summary label='รอคิว' value={waitingCount} />
          </div>
        </div>

        <div className='panel-line mb-8 grid gap-5 rounded-[28px] p-5 lg:grid-cols-[1fr_auto_auto]'>
          <label className='flex min-h-14 items-center gap-4 rounded-full border-2 border-[#202020] px-5'>
            <Search size={26} />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              className='w-full bg-transparent text-lg outline-none'
              placeholder='ค้นหาชื่อเพลงหรือผู้รีเควส'
            />
          </label>
          <SelectControl icon={<SlidersHorizontal size={22} />} value={tier} onChange={setTier} options={tiers} label='Tier' />
          <SelectControl value={status} onChange={setStatus} options={['All', 'Waiting', 'Done']} label='Status' />
        </div>

        <div className='panel-line overflow-hidden rounded-[28px]'>
          <table className='w-full min-w-[980px] border-collapse text-left text-lg'>
            <thead>
              <tr>
                <th className='border-b-2 border-[#202020] p-4'>Tier</th>
                <th className='border-b-2 border-[#202020] p-4'>คิว</th>
                <th className='border-b-2 border-[#202020] p-4'>เพลง</th>
                <th className='border-b-2 border-[#202020] p-4'>ผู้รีเควส</th>
                <th className='border-b-2 border-[#202020] p-4'>ลิงก์เพลง</th>
                <th className='border-b-2 border-[#202020] p-4 text-center'>แปลรึยัง?</th>
                <th className='border-b-2 border-[#202020] p-4'>Req.Date</th>
                <th className='border-b-2 border-[#202020] p-4'>Waiting Days</th>
              </tr>
            </thead>
            <tbody>
              {visibleItems.map((item, index) => (
                <tr key={`${item.tier}-${item.queueNumber}-${item.songTitle}`} className={`${index % 2 ? 'bg-[#f0f0ec]' : 'bg-white'} transition-colors hover:bg-[#e8f8ef]`}>
                  <td className='border-b border-[#202020]/25 p-4 font-bold'>{displayTier(item.tier)}</td>
                  <td className='border-b border-[#202020]/25 p-4 text-center font-bold'>{item.queueNumber ?? '-'}</td>
                  <td className='border-b border-[#202020]/25 p-4 font-black'>{item.songTitle}</td>
                  <td className='border-b border-[#202020]/25 p-4'>{item.requester ?? '-'}</td>
                  <td className='border-b border-[#202020]/25 p-4'>
                    {item.songUrl ? (
                      <a href={item.songUrl} target='_blank' rel='noreferrer' className='inline-flex items-center gap-2 rounded-full bg-[#ececec] px-3 py-1 font-bold'>
                        <Youtube size={16} className='text-red-600' fill='currentColor' />
                        เปิดเพลง
                      </a>
                    ) : '-'}
                  </td>
                  <td className='border-b border-[#202020]/25 p-4'>
                    <span className={`mx-auto flex h-8 w-8 items-center justify-center rounded-md border-2 ${item.translated ? 'border-[#08c765] bg-[#08c765] text-white' : 'border-[#9b9ba2] bg-white text-[#9b9ba2]'}`}>
                      {item.translated ? <Check size={20} /> : <Clock size={18} />}
                    </span>
                  </td>
                  <td className='border-b border-[#202020]/25 p-4'>{item.requestDate ?? 'N/A'}</td>
                  <td className='border-b border-[#202020]/25 p-4 font-bold'>{formatWaiting(item)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className='mt-8 flex justify-end'>
          <a href='https://www.youtube.com/@FCNami_TT' target='_blank' rel='noreferrer' className='pill-button'>
            Request on YouTube
            <ArrowRight size={24} />
          </a>
        </div>
      </section>
    </Layout>
  );
}

function Summary({ label, value }: { label: string; value: number }) {
  return (
    <div className='rounded-2xl border-2 border-[#202020] bg-white px-6 py-4'>
      <div className='text-3xl font-black'>{value}</div>
      <div className='text-sm font-bold text-[#77777d]'>{label}</div>
    </div>
  );
}

function SelectControl({
  icon,
  label,
  options,
  value,
  onChange,
}: {
  icon?: ReactNode;
  label: string;
  options: string[];
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className='flex min-h-14 items-center gap-3 rounded-full border-2 border-[#202020] px-5'>
      {icon}
      <span className='font-bold text-[#69696f]'>{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)} className='bg-transparent font-black outline-none'>
        {options.map((option) => (
          <option key={option} value={option}>{option}</option>
        ))}
      </select>
    </label>
  );
}

function displayTier(tier: string) {
  return tier.replace(/\+$/u, '').trim();
}

function formatWaiting(item: QueueItemResponse) {
  if (item.translated) return 'Done';
  if (item.waitingDays == null) return '-';
  return `${item.waitingDays} วัน`;
}

