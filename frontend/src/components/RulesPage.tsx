import Layout from './Layout';
import { ArrowRight, MessageCircle, RefreshCw, Search, Youtube } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { ReactNode } from 'react';

export default function RulesPage() {
  const rules = [
    'คอมเมนต์ชื่อเพลงและลิงก์ YouTube ใต้คลิปล่าสุดของช่อง เพราะเจ้าของช่องเห็นง่ายที่สุด',
    'ถ้าขอมากกว่า 1 เพลง เพลงถัดไปจะเข้า 2nd Request, 3rd Request และ tier ถัดไปตามลำดับ',
    'รอบทำคลิปใช้ Main Road 4 คลิป แล้วสลับกับเพลงตามใจเจ้าของช่อง 1 คลิป',
    'เมื่อเพลงใน Main Road แปลแล้ว เพลงจาก 2nd Request จะเลื่อนขึ้นมาต่อท้าย Main Road',
    'เพลงใน tier ลึกกว่าจะขยับขึ้นตามลำดับเมื่อ tier ด้านบนว่าง',
    'ถ้าเพลงที่อยากขอมีอยู่แล้วใน tier ลึกกว่า สามารถดึงเพลงนั้นขึ้นมาเป็นคิวของตัวเองได้',
    'เว็บนี้แสดงคิวให้อ่านง่าย แต่ Google Sheet ยังเป็น source of truth ของคิวรีเควส',
  ];

  return (
    <Layout>
      <section className='pb-20 pt-8'>
        <div className='mb-12 flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between'>
          <div>
            <h1 className='outline-title text-[clamp(4rem,7vw,8.4rem)] font-black uppercase leading-none'>REQUESTS RULES</h1>
            <p className='mt-5 max-w-4xl text-xl leading-relaxed text-[#69696f]'>
              ขอเพลงผ่าน YouTube comment แล้วกลับมาเช็กสถานะที่หน้า Queue ได้ในเว็บนี้
            </p>
          </div>
          <div className='flex flex-wrap gap-3'>
            <a href='https://www.youtube.com/@FCNami_TT' target='_blank' rel='noreferrer' className='pill-button'>
              <Youtube size={24} />
              Comment on YouTube
              <ArrowRight size={24} />
            </a>
            <Link to='/queue' className='pill-button bg-[#08c765] text-black hover:bg-[#06b65c]'>
              View Queue
              <ArrowRight size={24} />
            </Link>
          </div>
        </div>

        <div className='mb-10 grid gap-5 lg:grid-cols-3'>
          <FlowCard icon={<MessageCircle />} title='1. Comment' body='ส่งชื่อเพลงและลิงก์ใน YouTube comment' />
          <FlowCard icon={<Search />} title='2. Check Queue' body='ค้นหาชื่อเพลงหรือชื่อผู้รีเควสในหน้า Queue' />
          <FlowCard icon={<RefreshCw />} title='3. Move Up' body='เพลงใน tier ลึกจะเลื่อนขึ้นเมื่อ Main Road ว่าง' />
        </div>

        <div className='panel-line mb-10 overflow-hidden rounded-[28px]'>
          <table className='w-full text-left'>
            <tbody>
              {rules.map((rule, i) => (
                <tr key={rule} className='border-b border-[#202020]/20 last:border-0 hover:bg-[#f0f0ec]'>
                  <td className='flex items-start gap-6 p-6'>
                    <div className='flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-[#e8c4b9] font-black'>
                      {i + 1}
                    </div>
                    <span className='text-lg font-medium leading-relaxed text-[#303035]'>{rule}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className='rounded-[28px] bg-[#e8c4b9] p-8 text-xl font-bold text-[#4b4b51] lg:ml-auto lg:max-w-[78%]'>
          สุดท้ายนี้ขอบคุณที่รีเควสให้ช่องเราแปลนะ งงหรือสงสัยตรงไหนสามารถแจ้งใต้คอมเมนต์ของคลิปที่ชอบได้เลย เดี๋ยวเราอธิบายเพิ่มให้
        </div>
      </section>
    </Layout>
  );
}

function FlowCard({ icon, title, body }: { icon: ReactNode; title: string; body: string }) {
  return (
    <div className='panel-line rounded-[28px] p-6'>
      <div className='mb-6 flex h-12 w-12 items-center justify-center rounded-full bg-[#202020] text-white'>{icon}</div>
      <h2 className='mb-2 text-2xl font-black'>{title}</h2>
      <p className='text-lg leading-relaxed text-[#69696f]'>{body}</p>
    </div>
  );
}
