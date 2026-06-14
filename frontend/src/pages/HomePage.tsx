import Hero from "@/components/home/Hero";

const HomePage = () => {
  return (
    <div className="bg-white min-h-screen">
      <main>
        <Hero />
        
        {/* Footer (Optional, based on ref) */}
        <footer className="py-8 text-center text-slate-300 text-xs bg-white border-t border-slate-50">
          <p>© 2026 FCNami T_T. All rights reserved.</p>
        </footer>
      </main>
    </div>
  );
};

export default HomePage;
