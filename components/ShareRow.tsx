import { AskLink } from "@/components/AskLink";
import { CopyLink, UrlLine } from "@/components/CopyLink";

export function ShareRow({
  message,
  path,
  name,
}: {
  message: string;
  path: string;
  name: string;
}) {
  return (
    <div className="share">
      <div className="share-actions">
        <AskLink message={message} />
        <CopyLink path={path} name={name} />
      </div>
      <UrlLine path={path} />
    </div>
  );
}
