package com.evolutiongaming.kafka.flow.cassandra

import cats.effect.Clock
import cats.syntax.all._
import com.evolutiongaming.cassandra.sync.CassandraSync
import com.evolutiongaming.catshelper.MonadThrowable
import com.evolutiongaming.kafka.flow.KafkaKey
import com.evolutiongaming.kafka.flow.journal.CassandraJournals
import com.evolutiongaming.kafka.flow.key.CassandraKeys
import com.evolutiongaming.kafka.flow.persistence.PersistenceModule
import com.evolutiongaming.kafka.flow.snapshot.CassandraSnapshots
import com.evolutiongaming.kafka.flow.snapshot.KafkaSnapshot
import com.evolutiongaming.kafka.journal.ConsRecord
import com.evolutiongaming.kafka.journal.FromBytes
import com.evolutiongaming.kafka.journal.ToBytes
import com.evolutiongaming.kafka.journal.eventual.cassandra.CassandraSession

trait CassandraPersistence[F[_], S] extends PersistenceModule[F, KafkaKey, KafkaSnapshot[S], ConsRecord]
object CassandraPersistence {

  /** Creates schema in Cassandra if not there yet */
  def withSchema[F[_]: MonadThrowable: Clock, S](
    session: CassandraSession[F],
    sync: CassandraSync[F]
  )(implicit
    fromBytes: FromBytes[F, S],
    toBytes: ToBytes[F, S]
  ): F[PersistenceModule[F, KafkaKey, KafkaSnapshot[S], ConsRecord]] = for {
    _keys      <- CassandraKeys.withSchema(session, sync)
    _journals  <- CassandraJournals.withSchema(session, sync)
    _snapshots <- CassandraSnapshots.withSchema[F, S](session, sync)
  } yield new CassandraPersistence[F, S] {
    def keys = _keys
    def journals = _journals
    def snapshots = _snapshots
  }

}