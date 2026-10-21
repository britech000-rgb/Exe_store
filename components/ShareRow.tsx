import { AskLink } from "@/components/AskLink";
import { ShareMenu } from "@/components/ShareMenu";

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
        <ShareMenu path={path} name={name} />
      </div>
    </div>
  );
}
